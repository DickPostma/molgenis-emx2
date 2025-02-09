const initialCollectionColumns = [
  { label: "Id:", column: "id", type: "string", showCopyIcon: true },
  { label: "Website:", column: "url", type: "hyperlink" },
  {
    label: "Quality labels:",
    column: {
      quality: [
        "label",
        "certification_report",
        "certification_image_link",
        "certification_number",
      ],
    },
    type: "quality",
    showOnBiobankCard: true,
  },
  {
    label: "Size:",
    column: { order_of_magnitude: ["label"] },
    type: "object",
    property: "label",
  },
  {
    label: "Available:",
    column: "size",
    type: "int",
    suffix: "samples",
    showOnBiobankCard: true,
  },
  {
    label: "Donor size:",
    column: { order_of_magnitude_donors: ["label"] },
    type: "object",
    property: "donors",
  },
  {
    label: "Donors:",
    column: "number_of_donors",
    type: "int",
    suffix: "donors",
  },
  {
    label: "Age:",
    type: "range",
    min: "age_low",
    max: "age_high",
    unit: "age_unit",
    unit_column: { age_unit: ["label"] },
  },
  {
    label: "Type:",
    column: { type: ["label"] },
    type: "array",
    showOnBiobankCard: true,
  },
  { label: "Sex:", column: { sex: ["label"] }, type: "array" },
  {
    label: "Materials:",
    column: { materials: ["name", "label", "ontologyTermURI", "code"] },
    type: "array",
    showOnBiobankCard: true,
  },
  {
    label: "Storage:",
    column: { storage_temperatures: ["label"] },
    type: "categoricalmref",
  },
  {
    label: "Data:",
    column: { data_categories: ["label", "code", "ontologyTermURI"] },
    type: "mref",
  },
  {
    label: "Diagnosis:",
    column: { diagnosis_available: ["code", "label", "ontologyTermURI"] },
    type: "mref",
  },
  {
    label: "Data use conditions:",
    column: { data_use: ["label", "ontologyTermURI"] },
    type: "array",
  },
  {
    column: [
      "name",
      "size",
      "description",
      "biobank.id",
      "biobank.name",
      "biobank.withdrawn",
      "biobank.url",
      "biobank.juridical_person",
      "biobank.contact.first_name",
      "biobank.contact.last_name",
      "biobank.contact.email",
      "biobank.contact.role",
      "contact.first_name",
      "contact.last_name",
      "contact.email",
      "contact.role",
      "contact.country.label",
      "head.first_name",
      "head.last_name",
      "head.role",
      "country.label",
      "network.name",
      "network.id",
      "url",
      "withdrawn",
      "parent_collection.id",
      "parent_collection.name",
    ],
  },
];

export default initialCollectionColumns;
