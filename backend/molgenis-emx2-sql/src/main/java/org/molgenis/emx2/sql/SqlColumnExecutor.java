package org.molgenis.emx2.sql;

import static org.jooq.impl.DSL.*;
import static org.molgenis.emx2.ColumnType.*;
import static org.molgenis.emx2.sql.MetadataUtils.saveColumnMetadata;
import static org.molgenis.emx2.sql.SqlColumnRefArrayExecutor.createRefArrayConstraints;
import static org.molgenis.emx2.sql.SqlColumnRefArrayExecutor.removeRefArrayConstraints;
import static org.molgenis.emx2.sql.SqlColumnRefBackExecutor.createRefBackColumnConstraints;
import static org.molgenis.emx2.sql.SqlColumnRefBackExecutor.removeRefBackConstraints;
import static org.molgenis.emx2.sql.SqlColumnRefExecutor.createRefConstraints;
import static org.molgenis.emx2.sql.SqlTypeUtils.getPsqlType;

import java.util.List;
import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.Table;
import org.molgenis.emx2.Column;
import org.molgenis.emx2.MolgenisException;
import org.molgenis.emx2.Reference;

public class SqlColumnExecutor {
  private SqlColumnExecutor() {
    // hide
  }

  public static void executeSetRequired(DSLContext jooq, Column column) {
    boolean isRequired = column.isRequired();
    switch (column.getColumnType()) {
      case REFBACK:
        // refback is never required so fix here
        isRequired = false;
        break;
      case REF:
      case REF_ARRAY:
        isRequired = column.getReferences().stream().allMatch(Reference::isRequired) && isRequired;
        break;
      default:
        // if has default, we will first update all 'null' to default
        if (column.isRequired() && column.getDefaultValue() != null) {
          jooq.update(column.getJooqTable())
              .set(column.getJooqField(), column.getDefaultValue())
              .where(column.getJooqField().isNull())
              .execute();
        }
        break;
    }
    // in case of FILE we have to add all parts
    if (FILE.equals(column.getColumnType())) {
      for (Field f : column.getJooqFileFields()) {
        executeSetRequired(jooq, column.getJooqTable(), f, column.isRequired());
      }
    }
    // simply add set nullability
    else {
      if (column.isReference()) {
        for (Reference ref : column.getReferences()) {
          executeSetRequired(jooq, column.getJooqTable(), ref.getJooqField(), isRequired);
        }
      } else {
        executeSetRequired(jooq, column.getJooqTable(), column.getJooqField(), isRequired);
      }
    }
  }

  private static void executeSetRequired(
      DSLContext jooq, Table table, Field field, boolean required) {
    if (required) {
      jooq.alterTable(table).alterColumn(field).setNotNull().execute();
    } else {
      jooq.alterTable(table).alterColumn(field).dropNotNull().execute();
    }
  }

  public static String getJoinTableName(Column column) {
    return column.getTable().getTableName() + "-" + column.getName();
  }

  static void executeAlterName(DSLContext jooq, Column oldColumn, Column newColumn) {
    // asumes validated before
    if (!oldColumn.getName().equals(newColumn.getName())) {
      if (FILE.equals(newColumn.getColumnType())) {
        for (String suffix : new String[] {"", "_extension", "_size", "_contents", "_mimetype"}) {
          jooq.execute(
              "ALTER TABLE {0} RENAME COLUMN {1} TO {2}",
              newColumn.getJooqTable(),
              field(name(oldColumn.getName() + suffix)),
              field(name(newColumn.getName() + suffix)));
        }
      } else {
        jooq.execute(
            "ALTER TABLE {0} RENAME COLUMN {1} TO {2}",
            newColumn.getJooqTable(),
            field(name(oldColumn.getName())),
            field(name(newColumn.getName())));
      }
    }
  }

  static void executeAlterType(DSLContext jooq, Column oldColumn, Column newColumn) {
    Table table = newColumn.getTable().getJooqTable();

    if (oldColumn.getColumnType().equals(newColumn.getColumnType())) {
      return; // nothing to do
    }

    // catch cases we do not support
    if (FILE.equals(oldColumn.getColumnType()) && !FILE.equals(newColumn.getColumnType())
        || !FILE.equals(oldColumn.getColumnType()) && FILE.equals(newColumn.getColumnType())) {
      throw new MolgenisException(
          "Alter type for column '"
              + newColumn.getName()
              + "' failed: Cannot convert from or to binary");
    }

    // pre changes
    if (REF_ARRAY.equals(oldColumn.getColumnType())) {
      // if ref_array drop the index
      jooq.execute(
          "DROP INDEX {0}",
          name(oldColumn.getSchemaName(), table.getName() + "/" + oldColumn.getName()));
    }

    // change the type
    alterField(
        jooq,
        table,
        oldColumn.getName(),
        oldColumn.getJooqField().getDataType(),
        newColumn.getJooqField().getDataType(),
        getPsqlType(newColumn));

    // post changes
    if (REF_ARRAY.equals(newColumn.getColumnType())) {
      executeCreateRefArrayIndex(jooq, table, newColumn.getJooqField());
    }
  }

  static void alterField(
      DSLContext jooq,
      Table table,
      String columnName,
      DataType oldType,
      DataType newType,
      String postgresType) {

    // change the raw type
    if (!newType.equals(oldType)) {
      if (newType.isArray() && !oldType.isArray()) {
        jooq.execute(
            "ALTER TABLE {0} ALTER COLUMN {1} TYPE {2} USING array[{1}::{3}]",
            table,
            name(columnName),
            keyword(postgresType),
            keyword(postgresType.replace("[]", ""))); // non-array type needed
      } else {
        jooq.execute(
            "ALTER TABLE {0} ALTER COLUMN {1} TYPE {2} USING {1}::{2}",
            table, name(columnName), keyword(postgresType));
      }
    }
  }

  static void reapplyRefbackContraints(Column oldColumn, Column newColumn) {
    if ((REF.equals(oldColumn.getColumnType()) || REF_ARRAY.equals(oldColumn.getColumnType()))
        && (REF.equals(newColumn.getColumnType()) || REF_ARRAY.equals(newColumn.getColumnType()))) {
      for (Column check : oldColumn.getRefTable().getColumns()) {
        if (REFBACK.equals(check.getColumnType())
            && oldColumn.getName().equals(check.getRefBack())) {
          check.getTable().dropColumn(check.getName());
          check.getTable().add(check);
        }
      }
    }
  }

  static void executeRemoveRefback(Column oldColumn, Column newColumn) {
    if ((REF.equals(oldColumn.getColumnType()) || REF_ARRAY.equals(oldColumn.getColumnType()))
        && !(REF.equals(newColumn.getColumnType())
            || REF_ARRAY.equals(newColumn.getColumnType()))) {
      for (Column check : oldColumn.getRefTable().getColumns()) {
        if (REFBACK.equals(check.getColumnType())
            && oldColumn.getName().equals(check.getRefBack())) {
          check.getTable().dropColumn(check.getName());
        }
      }
    }
  }

  static void executeCreateColumn(DSLContext jooq, Column column) {
    String current = column.getName(); // for composite ref errors
    try {
      // create the column
      if (column.isReference()) {
        for (Reference ref : column.getReferences()) {
          current = ref.getName();
          // check if reference name already exists, composite ref may reuse columns
          // either other column, or a part of a reference
          if (!ref.isOverlapping()) {
            jooq.alterTable(column.getJooqTable()).addColumn(ref.getJooqField()).execute();
            if (REF_ARRAY.equals(column.getColumnType())) {
              executeCreateRefArrayIndex(jooq, column.getJooqTable(), ref.getJooqField());
            }
          }
        }

        // we only have hard not null in case of primary key
        if (column.isPrimaryKey()) {
          executeSetRequired(jooq, column);
        }
      } else if (FILE.equals(column.getColumnType())) {
        for (Field f : column.getJooqFileFields()) {
          jooq.alterTable(column.getJooqTable()).addColumn(f).execute();
        }
      } else {
        jooq.alterTable(column.getJooqTable()).addColumn(column.getJooqField()).execute();
        executeSetDefaultValue(jooq, column);

        // we only have hard not null in case of primary key
        if (column.isPrimaryKey()) {
          executeSetRequired(jooq, column);
        }
      }
      // central constraints
      SqlTableMetadataExecutor.updateSearchIndexTriggerFunction(
          jooq, column.getTable(), column.getTableName());
      saveColumnMetadata(jooq, column);
    } catch (Exception e) {
      if (e.getMessage() != null && e.getMessage().contains("null values")) {
        throw new MolgenisException(
            "Create column '"
                + column.getTableName()
                + "."
                + current
                + "' failed:"
                + e.getMessage()
                + ". You might want to set nullable=TRUE or add a default value to update existing rows.");
      } else {
        throw new MolgenisException(
            "Create column '" + column.getTableName() + "." + current + "' failed", e);
      }
    }
  }

  static void validateColumn(Column c) {
    if (c.getName() == null) {
      throw new MolgenisException("Add column failed: Column name cannot be null");
    }
    if (c.getKey() > 0 && c.getTable().getKeyFields(c.getKey()).size() > 1 && !c.isRequired()) {
      throw new MolgenisException(
          "unique on column '"
              + c.getTableName()
              + "."
              + c.getName()
              + "' failed: When key spans multiple columns, none of the columns can be nullable");
    }
    if (c.isReference() && c.getRefTable() == null) {
      throw new MolgenisException(
          "Add column '"
              + c.getTableName()
              + "."
              + c.getName()
              + "' failed: 'refTable' required for columns of type REF, REF_ARRAY, REFBACK");
    }
    if (c.getRefLink() != null) {
      if (c.getTable().getColumn(c.getRefLink()) == null) {
        throw new MolgenisException(
            "Add column '"
                + c.getTableName()
                + "."
                + c.getName()
                + "' failed: refLink '"
                + c.getRefLink()
                + "' column cannot be found");
      }
      if (!List.of(REF, REF_ARRAY)
          .contains(c.getTable().getColumn(c.getRefLink()).getColumnType())) {
        throw new MolgenisException(
            "Add column '"
                + c.getTableName()
                + "."
                + c.getName()
                + "' failed: refLink "
                + c.getRefLink()
                + " is not a REF,REF_ARRAY");
      }
    }
    // fix required
    if (c.getKey() == 1 && !c.isRequired()) {
      c.setRequired(true);
    }
  }

  private static void executeCreateRefArrayIndex(DSLContext jooq, Table table, Field field) {
    jooq.execute(
        "CREATE INDEX {0} ON {1} USING GIN( {2} )",
        name(table.getName() + "/" + field.getName()), table, field);
  }

  static void executeCreateRefConstraints(DSLContext jooq, Column column) {
    // set constraints
    switch (column.getColumnType()) {
      case REF:
        createRefConstraints(jooq, column);
        break;
      case REF_ARRAY:
        createRefArrayConstraints(jooq, column);
        break;
        //      case MREF:
        //        createMrefConstraints(jooq, column);
        //        break;
      case REFBACK:
        createRefBackColumnConstraints(jooq, column);
        break;
      default:
        break;
    }
  }

  static void executeRemoveColumn(DSLContext jooq, Column column) {
    executeRemoveRefConstraints(jooq, column);
    if (FILE.equals(column.getColumnType())) {
      for (Field f : column.getJooqFileFields()) {
        jooq.alterTable(SqlTableMetadataExecutor.getJooqTable(column.getTable()))
            .dropColumnIfExists(f)
            .execute();
      }
    } else if (column.isReference()) {
      for (Reference ref : column.getReferences()) {
        // check if reference name already exists, composite ref may reuse columns
        // either other column, or a part of a reference
        if (!ref.isOverlapping()) {
          jooq.alterTable(column.getJooqTable()).dropColumn(ref.getJooqField()).execute();
        }
      }
    } else {
      jooq.alterTable(SqlTableMetadataExecutor.getJooqTable(column.getTable()))
          .dropColumnIfExists(field(name(column.getName())))
          .execute();
    }
    MetadataUtils.deleteColumn(jooq, column);
  }

  static void executeRemoveRefConstraints(DSLContext jooq, Column column) {
    // remove triggers
    switch (column.getColumnType()) {
      case REF:
        SqlColumnRefExecutor.removeRefConstraints(jooq, column);
        break;
      case REF_ARRAY:
        removeRefArrayConstraints(jooq, column);
        break;
      case REFBACK:
        removeRefBackConstraints(jooq, column);
        break;
        //      case MREF:
        //        dropMrefConstraints(jooq, column);
        //        break;
      default:
        // nothing to do
    }
  }

  public static void executeSetDefaultValue(DSLContext jooq, Column newColumn) {
    if (newColumn.getDefaultValue() != null) {
      jooq.alterTable(newColumn.getJooqTable())
          .alterColumn(newColumn.getJooqField())
          .defaultValue(newColumn.getDefaultValue())
          .execute();
    } else {
      // remove default
      jooq.alterTable(newColumn.getJooqTable())
          .alterColumn(newColumn.getJooqField())
          .dropDefault()
          .execute();
    }
  }
}
