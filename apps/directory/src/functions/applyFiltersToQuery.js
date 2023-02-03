/** sets filters on baseQuery, by reference */
export async function applyFiltersToQuery (baseQuery, filters) {
    baseQuery.resetAllFilters()
    const activeFilters = Object.keys(filters)

    if (activeFilters.length === 0) return

    for (const filterKey of activeFilters) {
        const filterValue = filters[filterKey]
        switch (filterKey) {
            case "search": {
                /** add filters to Biobanks */
                baseQuery.orWhere("id").like(filterValue)
                baseQuery.orWhere("name").like(filterValue)
                baseQuery.orWhere("acronym").like(filterValue)
                baseQuery.orWhere("collections.id").like(filterValue)
                baseQuery.orWhere("collections.name").like(filterValue)
                baseQuery.orWhere("collections.acronym").like(filterValue)
                baseQuery.orWhere("collections.diagnosisAvailable.name").like(filterValue)
                baseQuery.orWhere("collections.diagnosisAvailable.code").like(filterValue)
                baseQuery.orWhere("collections.diagnosisAvailable.label").like(filterValue)
                baseQuery.orWhere("collections.diagnosisAvailable.definition").like(filterValue)
                baseQuery.orWhere("collections.materials.name").like(filterValue)
                baseQuery.orWhere("collections.materials.label").like(filterValue)

                /** and filter the collections  */
                baseQuery.filter("collections", "id").like(filterValue)
                baseQuery.filter("collections", "name").like(filterValue)
                baseQuery.filter("collections", "acronym").like(filterValue)
                baseQuery.filter("collections", "diagnosisAvailable.name").like(filterValue)
                baseQuery.filter("collections", "diagnosisAvailable.code").like(filterValue)
                baseQuery.filter("collections", "diagnosisAvailable.label").like(filterValue)
                baseQuery.filter("collections", "diagnosisAvailable.definition").like(filterValue)
                baseQuery.filter("collections", "materials.name").like(filterValue)
                baseQuery.filter("collections", "materials.label").like(filterValue)
            }
        }
    }
}

// TODO: add the properties to the base query, 