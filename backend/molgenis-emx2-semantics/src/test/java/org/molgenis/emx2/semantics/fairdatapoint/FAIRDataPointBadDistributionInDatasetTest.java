package org.molgenis.emx2.semantics.fairdatapoint;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.molgenis.emx2.Database;
import org.molgenis.emx2.Row;
import org.molgenis.emx2.Schema;
import org.molgenis.emx2.Table;
import org.molgenis.emx2.datamodels.FAIRDataHubLoader;
import org.molgenis.emx2.fairdatapoint.FAIRDataPointDataset;
import org.molgenis.emx2.sql.TestDatabaseFactory;
import spark.Request;

/**
 * FDP Dataset must have 1+ distributions, in EMX2 represented by 1 table name. The FDP
 * Distributions endpoint 'generates' distributions by combining table name with different formats.
 * Because FDP Dataset 'distribution' is a table name, it is String datatype, and may not match an
 * actual table. This must be checked at the FDP Dataset endpoint. Technically it could also
 * invalidate the overarching FDP Catalog, which must have 1+ datasets. But since this is a 'dataset
 * problem', we leave it at that level.
 */
public class FAIRDataPointBadDistributionInDatasetTest {

  static Database database;
  static Schema fairDataHub_baddistribution;

  @BeforeAll
  public static void setup() {
    database = TestDatabaseFactory.getTestDatabase();
    fairDataHub_baddistribution = database.dropCreateSchema("fairDataHub_baddistribution");
    FAIRDataHubLoader fairDataHubLoader = new FAIRDataHubLoader();
    fairDataHubLoader.load(fairDataHub_baddistribution, true);
  }

  @Test
  public void FDPBadDistribution() throws Exception {

    // check correct situation: distribution value matches a table, API returns as normal
    Request request = mock(Request.class);
    when(request.url())
        .thenReturn(
            "http://localhost:8080/api/fdp/dataset/fairDataHub_baddistribution/datasetId01");
    when(request.params("id")).thenReturn("datasetId01");
    FAIRDataPointDataset fairDataPointDataset =
        new FAIRDataPointDataset(request, fairDataHub_baddistribution.getTable("FDP_Dataset"));
    String result = fairDataPointDataset.getResult();
    assertTrue(
        result.contains(
            "dcat:distribution <http://localhost:8080/api/fdp/distribution/fairDataHub_baddistribution/Analyses/csv>,"));

    // set distribution to a value that does NOT corresepond to a table
    Table table = fairDataHub_baddistribution.getTable("FDP_Dataset");
    Random random = new Random();
    for (Row row : table.retrieveRows()) {
      row.set("distribution", "something_quite_wrong_" + random.nextInt());
      table.update(row);
    }

    // API should check, find that distribution value does not match a table, and throw error
    when(request.url())
        .thenReturn(
            "http://localhost:8080/api/fdp/dataset/fairDataHub_baddistribution/datasetId01");
    when(request.params("id")).thenReturn("datasetId01");
    Exception exception =
        assertThrows(
            Exception.class,
            () ->
                new FAIRDataPointDataset(
                        request, fairDataHub_baddistribution.getTable("FDP_Dataset"))
                    .getResult());
    String expectedMessage =
        "Schema does not contain the requested table for distribution. Make sure the value of 'distribution' in your FDP_Dataset matches a table name (from the same schema) you want to publish.";
    String actualMessage = exception.getMessage();
    assertTrue(actualMessage.contains(expectedMessage));
  }
}
