import { defineStore } from "pinia";
import { computed, ref } from "vue";
import QueryEMX2 from "../functions/queryEMX2";
import { useSettingsStore } from "./settingsStore";

export const useBiobanksStore = defineStore("biobanksStore", () => {
  const settingsStore = useSettingsStore();
  const biobankCardColumns = settingsStore.config.biobankCardColumns;
  const biobankColumns = settingsStore.config.biobankColumns;
  const collectionColumns = settingsStore.config.collectionColumns;
  const graphqlEndpoint = settingsStore.config.graphqlEndpoint;
  const biobankCardGraphql = biobankCardColumns.map(
    (biobankColumn) => biobankColumn.column
  );

  const biobankGraphql = biobankColumns.map(
    (biobankColumn) => biobankColumn.column
  ).concat({ collections: collectionColumns.map(collectionColumn => collectionColumn.column) })
    .concat(["name"]);

  let biobankCards = ref([]);
  let waitingForResponse = ref(false);

  const baseQuery = new QueryEMX2(graphqlEndpoint)
    .table("Biobanks")
    .select([
      "id",
      "name",
      "collections.id",
      "collections.name",
      "collections.size",
      ...biobankCardGraphql,
    ]) // TODO: add different config for the collection side
    .orderBy("Biobanks", "name", "asc")
    .orderBy("collections", "id", "asc");

  /** GraphQL query to get all the data necessary for the home screen 'aka biobank card view */
  async function getBiobankCards () {
    waitingForResponse.value = true;
    if (biobankCards.value.length === 0) {
      const biobankResult = await baseQuery.execute();
      biobankCards.value = biobankResult.Biobanks;
    }
    waitingForResponse.value = false;
    return biobankCards.value;
  }

  async function getBiobankCard (id) {

    const biobankReportQuery = new QueryEMX2(graphqlEndpoint)
      .table("Biobanks")
      .select([
        "contact.first_name",
        "contact.last_name",
        "contact.email",
        "contact.country.label",
        "contact.role",
        "head.first_name",
        "head.last_name",
        "head.role",
        "country.label",
        "network.name",
        "network.id",
        "url",
        "withdrawn",
        ...biobankGraphql
      ])
      .orderBy("Biobanks", "name", "asc")
      .orderBy("collections", "id", "asc")
      .where("id")
      .like(id)
    return await biobankReportQuery.execute();
  }

  async function updateBiobankCards () {
    waitingForResponse.value = true;
    biobankCards.value = [];
    const biobankResult = await baseQuery.execute();

    /** only show biobanks that have collections */
    const foundBiobanks = biobankResult.Biobanks ? biobankResult.Biobanks.filter(
      (biobank) => biobank.collections
    ) : [];
    biobankCards.value = foundBiobanks;
    waitingForResponse.value = false;
  }

  const biobankCardsHaveResults = computed(() => {
    return !waitingForResponse.value && biobankCards.value.length > 0;
  });

  const waiting = computed(() => {
    return waitingForResponse.value;
  });

  const biobankCardsBiobankCount = computed(() => {
    return biobankCards.value.length;
  });

  const biobankCardsCollectionCount = computed(() => {
    return biobankCards.value
      .filter((bc) => bc.collections)
      .flatMap((biobank) => biobank.collections).length;
  });

  const biobankCardsSubcollectionCount = computed(() => {
    if (!biobankCards.value.length) return 0;
    const collections = biobankCards.value
      .filter((bc) => bc.collections)
      .flatMap((biobank) => biobank.collections);
    if (!collections.length) return 0;
    return collections
      .filter((c) => c.sub_collections)
      .flatMap((collection) => collection.sub_collections).length;
  });

  return {
    updateBiobankCards,
    getBiobankCards,
    getBiobankCard,
    waiting,
    biobankCardsHaveResults,
    biobankCardsBiobankCount,
    biobankCardsCollectionCount,
    biobankCardsSubcollectionCount,
    biobankCards,
    baseQuery,
  };
});
