// package org.molgenis.emx2.io.csv;
//
// import com.univocity.parsers.common.record.Record;
// import com.univocity.parsers.csv.CsvParser;
// import com.univocity.parsers.csv.CsvParserSettings;
// import org.molgenis.Row;
// import org.molgenis.beans.Row;
//
// import java.io.*;
// import java.util.Iterator;
// import java.util.LinkedHashMap;
//
/// ** Don't use this one, it runs out of memory if columns have more than 4096 chars */
// public class RowReaderUnivocity {
//
//  @Deprecated
//  /** slower than unbuffered */
//  public static Iterable<Row> readBuffered(File f) throws IOException {
//    return read(new BufferedReader(new FileReader(f)));
//  }
//
//  public static Iterable<Row> read(File f) throws IOException {
//    return read(new FileReader(f));
//  }
//
//  public static Iterable<Row> read(Reader in) throws IOException {
//    // https://www.univocity.com/pages/univocity_parsers_tutorial
//    CsvParserSettings settings = new CsvParserSettings();
//    settings.detectFormatAutomatically();
//    settings.setHeaderExtractionEnabled(true);
//    CsvParser parser = new CsvParser(settings);
//
//    return new Iterable<Row>() {
//      // ... some reference to data
//      public Iterator<Row> iterator() {
//        return new Iterator<Row>() {
//          final Iterator<Record> it = parser.iterateRecords(in).iterator();
//
//          public boolean hasNext() {
//            return it.hasNext();
//          }
//
//          public Row next() {
//            return new Row(it.next().fillFieldMap(new LinkedHashMap<>()));
//          }
//
//          public void remove() {
//            throw new UnsupportedOperationException();
//          }
//        };
//      }
//    };
//  }
// }
