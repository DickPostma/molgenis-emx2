import { defineStore } from 'pinia'
import { computed, ref } from 'vue';
import QueryEMX2 from '../functions/queryEMX2'
import { useSettingsStore } from './settingsStore'

export const useBiobanksStore = defineStore('biobanksStore', () => {
    const settingsStore = useSettingsStore();
    const biobankCardColumns = settingsStore.config.biobankCardColumns
    const graphqlEndpoint = settingsStore.config.graphqlEndpoint
    const biobankCardGraphql = biobankCardColumns.map(biobankColumn => biobankColumn.column)

    let biobankCards = ref([])
    let waitingForResponse = ref(false)

    const baseQuery = new QueryEMX2(graphqlEndpoint)
        .table('Biobanks')
        .select(['id', 'name', 'collections.id', 'collections.name', 'collections.size', ...biobankCardGraphql]) // TODO: add different config for the collection side
        .orderBy("Biobanks", "name", "asc")
        .orderBy("collections", "id", "asc")

    console.log(baseQuery.getQuery() , '<< base bio query')
    /** GraphQL query to get all the data necessary for the home screen 'aka biobank card view */
    async function getBiobankCards () {
        waitingForResponse.value = true
        if (biobankCards.value.length === 0) {

            const biobankResult = await baseQuery.execute()
            biobankCards.value = biobankResult.Biobanks
        }
        waitingForResponse.value = false
        return biobankCards.value
    }

    async function updateBiobankCards () {
        waitingForResponse.value = true
        biobankCards.value = []
        const biobankResult = await baseQuery.execute()

        const foundBiobanks = biobankResult.Biobanks
        biobankCards.value = foundBiobanks || []
        waitingForResponse.value = false
    }

    const biobankCardsHaveResults = computed(() => {
        return !waitingForResponse.value && biobankCards.value.length > 0
    })

    const waiting = computed(() => {
        return waitingForResponse.value
    })

    const biobankCardsBiobankCount = computed(() => {
        return biobankCards.value.length
    })

    const biobankCardsCollectionCount = computed(() => {
        return biobankCards.value.filter(bc => bc.collections).flatMap(biobank => biobank.collections).length
    })

    const biobankCardsSubcollectionCount = computed(() => {
        if (!biobankCards.value.length) return 0
        const collections = biobankCards.value.filter(bc => bc.collections).flatMap(biobank => biobank.collections)
        if (!collections.length) return 0
        return collections.filter(c => c.subcollections).flatMap(collection => collection.subcollections).length
    })


    return {
        updateBiobankCards,
        getBiobankCards,
        waiting,
        biobankCardsHaveResults,
        biobankCardsBiobankCount,
        biobankCardsCollectionCount,
        biobankCardsSubcollectionCount,
        biobankCards,
        baseQuery
    }
})
