package org.molgenis.emx2.beaconv2.responses.info;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.molgenis.emx2.beaconv2.responses.configuration.Organization;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class InfoResponse {
  String id = "molgenis";
  String name = "MOLGENIS Beacon v2";
  String apiVersion = "v2.0.0-draft.4";
  String environment = "prod";
  Organization organization = new Organization();
  String description = "This Beacon is based on the GA4GH Beacon v2.0";
  String version = "v2";
  String welcomeUrl = "https://www.molgenis.org/beacon";
  String createDateTime = "2022-01-01";
  String updateDateTime = "2022-01-01";
}
