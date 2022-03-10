package org.molgenis.emx2.beaconv2.common;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import java.util.Arrays;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class CommonMeta {

  String beaconId = "org.molgenis.beaconv2";
  String apiVersion = "v2.0.0-draft.4";
  String $schema;
  CommonSchemas[] returnedSchemas;

  /**
   * "returnedSchemas" refers to the schema of the current response, so always 1 (??) e.g.
   * "configuration", "map", etc.
   *
   * @param $schema
   * @param entityType
   */
  public CommonMeta(String $schema, String entityType) {
    this.$schema = $schema;
    this.returnedSchemas =
        Arrays.asList(new CommonSchemas(entityType)).toArray(CommonSchemas[]::new);
  }
}
