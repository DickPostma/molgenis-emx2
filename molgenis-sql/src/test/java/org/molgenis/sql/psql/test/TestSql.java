package org.molgenis.sql.psql.test;

import org.jooq.Field;
import org.junit.BeforeClass;
import org.junit.Test;
import org.molgenis.*;
import org.molgenis.beans.RowBean;
import org.molgenis.sql.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.fail;
import static org.molgenis.sql.SqlRow.MOLGENISID;
import static org.molgenis.Column.Type.*;

public class TestSql {
  private static Database db;

  @BeforeClass
  public static void setUp() throws MolgenisException, SQLException {
    db = SqlTestHelper.getEmptyDatabase();
    db.createSchema("TestSql");
  }

  @Test
  public void testBatch() throws MolgenisException {
    Schema s = db.getSchema("TestSql");
    Table test_batch = s.createTable("TestBatch");
    test_batch.addColumn("test", STRING);
    test_batch.addColumn("testint", INT);

    long startTime = System.currentTimeMillis();

    int size = 1000;

    List<Row> rows = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Row r = new RowBean();
      r.setString("test", "test" + i);
      r.setInt("testint", i);
      rows.add(r);
    }
    long endTime = System.currentTimeMillis();
    System.out.println(
        "Generated " + size + " test record in " + (endTime - startTime) + " milliseconds");

    startTime = System.currentTimeMillis();
    test_batch.insert(rows.subList(0, 100));
    endTime = System.currentTimeMillis();
    System.out.println("First batch insert " + (endTime - startTime) + " milliseconds");

    startTime = System.currentTimeMillis();
    test_batch.insert(rows.subList(100, 200));
    endTime = System.currentTimeMillis();
    System.out.println("Second batch insert " + (endTime - startTime) + " milliseconds");

    startTime = System.currentTimeMillis();
    test_batch.insert(rows.subList(200, 300));
    endTime = System.currentTimeMillis();
    System.out.println("Third batch insert " + (endTime - startTime) + " milliseconds");

    startTime = System.currentTimeMillis();
    for (Row r : rows) {
      r.setString("test", r.getString("test") + "_updated");
    }
    test_batch.update(rows);
    endTime = System.currentTimeMillis();
    System.out.println("Batch update " + (endTime - startTime) + " milliseconds");

    startTime = System.currentTimeMillis();
    for (Row r : s.query("TestBatch").retrieve()) {
      System.out.println(r);
    }
    endTime = System.currentTimeMillis();
    System.out.println("Retrieve " + (endTime - startTime) + " milliseconds");
  }

  @Test
  public void testCreate() throws MolgenisException {

    long startTime = System.currentTimeMillis();

    SqlTestHelper.emptyDatabase();
    Schema s = db.createSchema("TestSql");

    // create a fromTable
    String PERSON = "Person";
    Table t = s.createTable(PERSON);
    t.addColumn("First Name", STRING).setNullable(false); // default nullable=false but for testing
    t.addRef("Father", t).setNullable(true);
    t.addColumn("Last Name", STRING);
    t.addUnique("First Name", "Last Name");
    long endTime = System.currentTimeMillis();

    System.out.println(
        "Created fromTable: \n" + t.toString() + " in " + (endTime - startTime) + " milliseconds");

    // reinitialise database to see if it can recreate from background
    db = new SqlDatabase(SqlTestHelper.getDataSource());
    s = db.getSchema("TestSql");
    assertEquals(1, s.getTables().size());

    // insert
    startTime = System.currentTimeMillis();
    Table t2 = s.getTable(PERSON);
    List<Row> rows = new ArrayList<>();
    int count = 1000;
    for (int i = 0; i < count; i++) {
      rows.add(new RowBean().setString("Last Name", "Duck" + i).setString("First Name", "Donald"));
    }
    t2.insert(rows);
    endTime = System.currentTimeMillis();
    long total = (endTime - startTime);
    System.out.println(
        "Insert of "
            + count
            + " records took "
            + total
            + " milliseconds (that is "
            + (1000 * count / total)
            + " rows/sec)");

    // queryOld
    startTime = System.currentTimeMillis();
    Query q = s.query(PERSON);
    for (Row row : q.retrieve()) {
      System.out.println("QueryOld result: " + row);
    }
    endTime = System.currentTimeMillis();
    System.out.println("QueryOld took " + (endTime - startTime) + " milliseconds");
    System.out.println("QueryOld contents " + q);

    // delete
    startTime = System.currentTimeMillis();
    t2.delete(rows);
    endTime = System.currentTimeMillis();
    total = (endTime - startTime);
    System.out.println(
        "Delete took " + total + " milliseconds (that is " + (1000 * count / total) + " rows/sec)");

    assertEquals(0, s.query("Person").retrieve().size());

    assertEquals(2, t.getUniques().size());
    try {
      t.removeUnique(MOLGENISID);
      fail("you shouldn't be allowed to remove primary key unique constraint");
    } catch (Exception e) {
      // good stuff
    }
    t.removeUnique("Last Name", "First Name");
    assertEquals(1, t.getUniques().size());

    assertEquals(4, t.getColumns().size());
    try {
      t.removeColumn(MOLGENISID);
      fail("you shouldn't be allowed to remove primary key column");
    } catch (Exception e) {
      // good stuff
    }
    t.removeColumn("Father");
    assertEquals(3, t.getColumns().size());

    // drop a fromTable
    db.getSchema("TestSql").dropTable(t.getName());
    assertEquals(null, db.getSchema("TestSql").getTable("Person"));
    // make sure nothing was left behind in backend
    db = new SqlDatabase(SqlTestHelper.getDataSource());
    assertEquals(null, db.getSchema("TestSql").getTable("Person"));
  }
}
