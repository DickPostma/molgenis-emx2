package org.molgenis.emx2.web;

import static org.molgenis.emx2.json.JsonUtil.getWriter;
import static org.molgenis.emx2.web.MolgenisWebservice.getSchema;
import static spark.Spark.get;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.molgenis.emx2.beacon.requests.BeaconRequestBody;
import org.molgenis.emx2.beacon.responses.BeaconFilteringTermsResponse;
import org.molgenis.emx2.beaconv2.responses.*;
import org.molgenis.emx2.beaconv2_prev.ServiceInfo;
import spark.Request;
import spark.Response;

// is a beacon on level of database, schema or table?
public class BeaconApi {

  public static void create() {

    // framework
    get("/:schema/api/beacon", BeaconApi::getInfo);
    get("/:schema/api/beacon/info", BeaconApi::getInfo);
    get("/:schema/api/beacon/service-info", BeaconApi::getInfo);
    get("/:schema/api/beacon/configuration", BeaconApi::getConfiguration);
    get("/:schema/api/beacon/map", BeaconApi::getMap);
    get("/:schema/api/beacon/entry_types", BeaconApi::getEntryTypes);
    get("/:schema/api/beacon/datasets", BeaconApi::getDatasets);
    get("/:schema/api/beacon/g_variants", BeaconApi::getGenomicVariants);

    //    get("/:schema/api/beacon/filtering_terms", BeaconApi::getFilteringTerms);
    //
    //    // datasets model
    //    get("/:schema/api/beacon/datasets", BeaconApi::getDatasets);
    //    get("/:schema/api/beacon/datasets/:table", BeaconApi::getDatasetsForTable);
    //    // these are the interesting queries
    //    post("/:schema/api/beacon/datasets", BeaconApi::postDatasets);
    //    post("/:schema/api/beacon/datasets/:table", BeaconApi::postDatasetsForTable);
  }

  private static String getInfo(Request req, Response res) throws JsonProcessingException {

    return getWriter().writeValueAsString(new Info());
  }

  private static String getServiceInfo(Request request, Response response)
      throws JsonProcessingException {
    return getWriter().writeValueAsString(new ServiceInfo());
  }

  private static Object getConfiguration(Request request, Response response)
      throws JsonProcessingException {
    return getWriter().writeValueAsString(new Configuration());
  }

  private static Object getMap(Request request, Response response) throws JsonProcessingException {
    return getWriter().writeValueAsString(new Map());
  }

  private static Object getEntryTypes(Request request, Response response)
      throws JsonProcessingException {
    return getWriter().writeValueAsString(new EntryTypes());
  }

  private static String getFilteringTerms(Request request, Response response)
      throws JsonProcessingException {
    String skip = request.queryParams("skip");
    String limit = request.queryParams("limit");
    // TODO handle skip and limit
    return getWriter().writeValueAsString(new BeaconFilteringTermsResponse());
  }

  private static String getDatasets(Request request, Response response)
      throws JsonProcessingException {
    String skip = request.queryParams("skip");
    String limit = request.queryParams("limit");

    // TODO pass request to response to set limits, offsets etc
    // result should be BeaconBooleanResponse, BeaconCountResponse or BeaconCollectionResponse
    return getWriter().writeValueAsString(new Datasets(getSchema(request)));
  }

  private static String getGenomicVariants(Request request, Response response)
      throws JsonProcessingException {
    return getWriter().writeValueAsString(new GenomicVariants());
  }

  private static String postDatasets(Request request, Response response)
      throws JsonProcessingException {
    // should parse body into
    BeaconRequestBody requestBody = null; // todo

    // result should be BeaconBooleanResponse, BeaconCountResponse or BeaconCollectionResponse
    return getWriter().writeValueAsString(null);
  }

  private static Object getDatasetsForTable(Request request, Response response)
      throws JsonProcessingException {

    // result should be BeaconBooleanResponse, BeaconCountResponse or beaconResultsetsResponse
    return getWriter().writeValueAsString(null);
  }

  private static Object postDatasetsForTable(Request request, Response response)
      throws JsonProcessingException {

    // should parse body into
    BeaconRequestBody requestBody = null; // todo

    // result should be BeaconBooleanResponse, BeaconCountResponse or beaconResultsetsResponse
    return getWriter().writeValueAsString(null);
  }
}
