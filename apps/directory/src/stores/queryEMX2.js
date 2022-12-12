/**
 * Created by Jelmer Veen 2020
 */

import request from "graphql-request"

class QueryEMX2 {
    tableName = ''
    filters = {}
    column = ''
    parentColumn = ''
    _schemaTablesInformation = {}
    selection = []
    graphqlUrl = ''
    branch = 'root'

    /**
     * @param {string} graphqlUrl the endpoint to query
     */
    constructor(graphqlUrl) {
        this.graphqlUrl = graphqlUrl
    }

    table (tableName) {
        /** Tables are always PascalCase */
        this.tableName = this._toPascalCase(tableName)
        return this
    }

    /**
     * @param {string | string[]} columns 
     */
    select (columns) {
        const requestedColumns = Array.isArray(columns) ? columns : [columns]
        /** column names are always lowercase */
        requestedColumns.forEach(name => name.toLowerCase())
        this.selection = requestedColumns
        return this
    }

    /**
     * Builds a query for use in EMX2 GraphQL api
     * @param {string} tableName 
     * @param {string[]} columns 
     * @param {object[]} filters 
     * @returns GraphQL query string
     */
    async execute () {

        /** Fail fast */
        if (!this.tableName) throw Error('You need to provide ', this.tableName ? 'a table name' : 'columns')

        /** Add all columns that are not linked to other tables */
        if (this.selection.length === 0) {
            const columns = await this.getColumnsForTable(this.tableName)
            this.selection = columns
        }

        return await request(this.graphqlUrl, this.getQuery())
    }

    getQuery () {
        const tableFilters = this.filters.length ? `(filter: ${this.filters})` : ''

        return `{
            ${this.tableName}${tableFilters} {
               ${this.selection.join()}
              }
            }`
    }

    /**
     * Gets the table information for the current schema
     */
    async getSchemaTablesInformation () {

        if (Object.keys(this._schemaTablesInformation).length) return this._schemaTablesInformation

        const result = await request(this.graphqlUrl, `
        {
            _schema {
              tables {
                name,
                columns {
                name,
                columnType
                }
              }
            }
          }
        `)
        this._schemaTablesInformation = result._schema.tables

        return this._schemaTablesInformation
    }

    async getColumnsForTable (tableName) {

        await this.getSchemaTablesInformation()

        return this._schemaTablesInformation
            .find(table => table.name === tableName).columns
            .filter(column => !column.columnType.includes('REF') && !column.columnType.includes('ONTOLOGY'))
            .map(column => this._toCamelCase(column.name.replace(/\s+/g, '')))
    }

    /**
     * If you want to create a nested query, for example { collections: { name: { like: 'lifelines' } } }
     * then column = 'collections', nested column = 'name'
     * @param {string} column 
     * @param {string} nestedColumn 
     * @returns 
     */
    where (column, nestedColumn) {
        this.branch = 'root'
        /** always convert to lowercase, else api will error */
        this.column = nestedColumn ? this._toCamelCase(nestedColumn) : this._toCamelCase(column)
        this.parentColumn = nestedColumn ? this._toCamelCase(column) : ''
        return this
    }

    /**
     * Works as where, but then for nested properties
     * @param {string} column 
     * @param {string} nestedColumn 
     * @returns 
     */
    filter (column, nestedColumn) {
        this.branch = column
        return this.where(column, nestedColumn)
    }

    and (column, nestedColumn) {
        this.type = '_and'
        return this.where(column, nestedColumn)
    }

    or (column, nestedColumn) {
        this.type = '_or'
        return this.where(column, nestedColumn)
    }

    /** Text, String, Url, Int, Bool, Datetime Filter */
    equals (value) {
        const operator = 'equals'

        this._createFilter(operator, value)
        return this
    }
    /** Text, String, Url, Int, Bool, Datetime Filter */
    notEquals (value) {
        const operator = 'not_equals'

        this._createFilter(operator, value)
        return this
    }

    /** Text, String, Url, Filter */
    like (value) {
        const operator = 'like'

        return this._createFilter(operator, value)

    }
    /** Text, String, Url, Filter */
    notLike (value) {
        const operator = 'not_like'

        this._createFilter(operator, value)
        return this
    }
    /** Text, String, Url, Filter */
    triagramSearch (value) {
        const operator = 'triagram_search'

        this._createFilter(operator, value)
        return this
    }
    /** Text, String, Url, Filter */
    textSearch (value) {
        const operator = 'text_search'

        this._createFilter(operator, value)
        return this
    }

    /** Int, Datetime Filter */
    between (value) {
        const operator = 'between'

        this._createFilter(operator, value)
        return this
    }
    /** Int, Datetime Filter */
    notBetween (value) {
        const operator = 'not_between'
        this._createFilter(operator, value)
        return this
    }

    _toPascalCase (value) {
        return value[0].toUpperCase() + value.substring(1)
    }

    _toCamelCase (value) {
        return value[0].toLowerCase() + value.substring(1)
    }

    _createQuery (root, properties, filters) {
        let result = '';

        result += `${root}${filters.root} {\n`;

        /** Create a nested object to represent the branches and their properties */
        let branches = {};
        for (let property of properties) {
            let parts = property.split('.');
            let currentBranch = branches;

            /** Create nested objects for each part of the property path */
            for (let i = 0; i < parts.length - 1; i++) {
                let part = parts[i].trim();
                if (!currentBranch[part]) {
                    currentBranch[part] = {};
                }
                currentBranch = currentBranch[part];
            }

            /** Add the property to the innermost branch */
            let propertyName = parts[parts.length - 1].trim();
            if (propertyName.indexOf('.') >= 0) {
                /** If the property name has a period, it is a branch */
                currentBranch[propertyName] = {};
            } else {
                /** Otherwise, it is a property */
                /** Store the properties in a separate object from the branches */
                if (!currentBranch.properties) {
                    currentBranch.properties = {};
                }
                currentBranch.properties[propertyName] = true;
            }
        }

        /** Recursively generate the output string for the branches and their properties */
        function generateOutput (branches, indentationLevel) {
            let indentation = '   '.repeat(indentationLevel);

            /** Add properties first */
            if (branches.properties) {
                let properties = branches.properties;
                for (let propertyName in properties) {
                    if (properties[propertyName] === true) {
                        result += `${indentation}${propertyName},\n`;
                    }
                }
            }

            result = `${result.substring(0, result.length - 2)}\n`;
            result = result.substring();

            /** Add the branches and their properties */
            for (let branchName in branches) {
                let branch = branches[branchName];
                if (branchName !== 'properties') {
                    /** Only add branches, not properties */
                    result += `${indentation}${branchName}${filters[branchName] || ''} {\n`;

                    generateOutput(branch, indentationLevel + 1);

                    result += indentation + '}\n';
                }
            }
        }

        generateOutput(branches, 1);

        result += '}';

        return result;
    }

    /** Private function to create the correct filter syntax. */
    _createFilter (operator, value) {
        let columnFilter = `{ ${this.column}: { ${operator}: "${value}"} }`

        if (this.parentColumn.length > 0) {
            columnFilter = `{ ${this.parentColumn}: ${columnFilter} }`
        }

        if (this.filters[this.branch]) {
            /** need to remove the last }, add an _and / _or and stitch it together */
            this.filters[this.branch] = `${this.filters[this.branch].substring(0, this.filters[this.branch].length - 2)}, ${this.type}: ${columnFilter}}`
        }
        else {
            this.filters[this.branch] = columnFilter
        }
        this.column = ''
        this.parentColumn = ''
        this.type = '_and'

        return this
    }
}

export default QueryEMX2