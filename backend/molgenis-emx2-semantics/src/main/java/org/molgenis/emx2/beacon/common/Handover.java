package org.molgenis.emx2.beacon.common;

// https://github.com/ga4gh-beacon/beacon-framework-v2/blob/main/common/beaconCommonComponents.json#Handover
public class Handover {
  HandoverType handoverType;

  // An optional text including considerations on the handover link provided.
  String note;

  // URL endpoint to where the handover process could progress (in RFC\n3986 format)
  String url;

  private class HandoverType {
    // Handover type, as an Ontology_term object with CURIE syntax for the\n`id` value.
    String id;

    // This would be the \"preferred Label\" in the case of an ontology term.
    String label;
  }
}
