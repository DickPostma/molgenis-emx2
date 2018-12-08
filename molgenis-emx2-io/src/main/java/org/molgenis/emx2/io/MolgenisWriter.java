package org.molgenis.emx2.io;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.molgenis.emx2.EmxColumn;
import org.molgenis.emx2.EmxModel;
import org.molgenis.emx2.EmxTable;
import org.molgenis.emx2.EmxType;
import org.molgenis.emx2.io.format.MolgenisFileRow;
import org.molgenis.emx2.io.parsers.ColumnDefinition;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.molgenis.emx2.EmxTable.MOLGENISID;
import static org.molgenis.emx2.io.parsers.ColumnDefinition.*;

public class MolgenisWriter {

    public void writeCsv(EmxModel model, Writer writer) throws IOException {
        CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("table", "column", "definition", "description"));
        for (MolgenisFileRow row : convertModelToMolgenisFileRows(model)) {
            csvPrinter.printRecord(row.getTable(), row.getColumn(), row.getDefinition(), row.getDescription());
        }
        csvPrinter.close();
    }

    public List<MolgenisFileRow> convertModelToMolgenisFileRows(EmxModel model) {
        List<MolgenisFileRow> rows = new ArrayList<>();

        for (EmxTable table : model.getTables()) {
            if (table.getExtend() != null || table.getUniques().isEmpty()) {
                rows.add(convertTableToRow(table));
            }
            for (EmxColumn column : table.getColumns()) {
                //ignore internal ID
                if (!MOLGENISID.equals(column.getName())) {
                    rows.add(convertColumnToRow(table, column));
                }
            }
        }
        return rows;
    }

    private MolgenisFileRow convertColumnToRow(EmxTable table, EmxColumn column) {
        return new MolgenisFileRow(table.getName(), column.getName(), getDefinitionString(column), column.getDescription());
    }

    private String getDefinitionString(EmxColumn col) {
        List<ColumnDefinition> def = new ArrayList<>();

        if (!EmxType.STRING.equals(col.getType())) {
            ColumnDefinition d = ColumnDefinition.valueOf(col.getType());
            switch (d) {
                case STRING:
                    break;
                case INT:
                    break;
                case LONG:
                    break;
                case BOOL:
                    break;
                case DECIMAL:
                    break;
                case TEXT:
                    break;
                case DATE:
                    break;
                case DATETIME:
                    break;
                case SELECT:
                case MSELECT:
                case RADIO:
                case CHECKBOX:
                    String colName = "." + escapeRef(col.getRef().getName());
                    if (MOLGENISID.equals(col.getRef().getName())) colName = "";
                    d.setParameterValue(escapeRef(col.getRef().getTable().getName()) + colName);
                    break;
                case NILLABLE:
                    break;
                case DEFAULT:
                    break;
                case UNIQUE:
                    break;
                case READONLY:
                    break;
                case VISIBLE:
                    break;
                case VALIDATION:
                    break;
                case UUID:
                    break;
                case HYPERLINK:
                    break;
                case EMAIL:
                    break;
                case HTML:
                    break;
                case FILE:
                    break;
                case ENUM:
                    break;
            }

            def.add(d);
        }
        if (col.getNillable()) def.add(NILLABLE);
        if (col.getReadonly()) def.add(READONLY);
        if (col.getDefaultValue() != null) def.add(DEFAULT.setParameterValue(col.getDefaultValue()));
        if (col.getValidation() != null) def.add(VALIDATION.setParameterValue(escapeScript(col.getValidation())));
        if (col.getUnique()) def.add(UNIQUE);
        if (col.getVisible() != null) def.add(VISIBLE.setParameterValue(escapeScript(col.getVisible())));

        return join(def, " ");
    }

    private String escapeScript(String value) {
        return value.replace("(", "\\(").replace(")", "\\)");
    }

    private String escapeRef(String value) {
        return value.replace(".", "\\.").replace("(", "\\(").replace(")", "\\)");
    }

    private MolgenisFileRow convertTableToRow(EmxTable table) {
        return null;
    }

    private String join(Collection collection, String delimiter) {
        return (String) collection.stream()
                .map(Object::toString)
                .collect(Collectors.joining(delimiter));
    }
}
