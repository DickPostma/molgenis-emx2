package org.molgenis.emx2.beacon.configuration;

import java.util.Map;
import org.molgenis.emx2.beacon.common.EntryType;
import org.molgenis.emx2.beacon.common.Granularity;

public class BeaconConfigurationSchema {
  String $schema;
  MaturityAttributes maturityAttributes;
  SecurityAttributes securityAttributes;

  // this one is dynamic, depending on implemented models
  Map<String, EntryType> entryTypes;

  public static class MaturityAttributes {
    public enum ProductionStatus {
      DEV,
      TEST,
      PROD
    };

    ProductionStatus productionStatus;
  }

  public static class SecurityAttributes {
    public enum SecurityLevels {
      PUBLIC,
      REGISTERED,
      CONTROLLED
    };

    Granularity defaultGranularity;
    SecurityLevels[] securityLevels;
  }
}
