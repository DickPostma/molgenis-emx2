// package org.molgenis.emx2.io.csv;
//
// import org.apache.commons.csv.CSVFormat;
// import org.apache.commons.csv.CSVRecord;
// import org.molgenis.Row;
// import org.molgenis.beans.Row;
//
// import java.io.*;
// import java.util.Iterator;
//
// public class RowReaderApacheCommons {
//
//  /** Don't use, slower than unbuffered */
//  @Deprecated
//  public static Iterable<Row> readBuffered(File f) throws IOException {
//    return read(new BufferedReader(new FileReader(f)));
//  }
//
//  public static Iterable<Row> read(File f) throws IOException {
//    return read(new FileReader(f));
//  }
//
//  public static Iterable<Row> read(Reader in) throws IOException {
//    Iterable<CSVRecord> records =
//        CSVFormat.DEFAULT
//            .withFirstRecordAsHeader()
//            .withIgnoreHeaderCase()
//            .withIgnoreSurroundingSpaces()
//            .withTrim()
//            .withIgnoreEmptyLines(true)
//            .parse(in);
//
//    return new Iterable<Row>() {
//      // ... some reference to data
//      public Iterator<Row> iterator() {
//        return new Iterator<Row>() {
//          final Iterator<CSVRecord> it = records.iterator();
//
//          public boolean hasNext() {
//            return it.hasNext();
//          }
//
//          public Row next() {
//            return new Row(it.next().toMap());
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
