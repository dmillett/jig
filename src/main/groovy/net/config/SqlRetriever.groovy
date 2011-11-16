package net.config

import groovy.sql.Sql

/**
 * In order to isolate config retrieval from the database. It performs
 * the following statement:
 *
 * "SELECT * FROM TABLE_FOO" and grabs columns 2, 3 to populate
 * key:value pairs in a map (assumes column 1 is the identity column).
 *
 * @author dmillett
 *
 * Copyright 2011 David Millett
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 *  limitations under the License.
 */
class SqlRetriever {

    /**
     * Look up all the key-value property style configs for each row in the
     * database listed above and load into a map.
     *
     * @param sqlTableInfos Ordered submap of 5 key-value pairs
     * @return A map of maps where each table name has a corresponding map with key:value pair(s)
     */
    def Map<String, Map<String, String>> loadFromDatabase(String tableName, Sql sql) {

        def tableMap = new HashMap<String, Map<String, String>>()

        try
        {
            // GString here causes SQL prepared statement path and error with ?
            // instead of the actual table name. Use java.lang.String instead
            def select = "SELECT * FROM " + tableName.toUpperCase()
            def dbConfigs = new HashMap<String, String>()
            def result = sql.rows(select)

            for ( rowResult in result )
            {
                // skip the identity column @ 0
                dbConfigs.put(rowResult.getAt(1), rowResult.getAt(2))
            }

            tableMap.put(tableName, dbConfigs)
        }
        catch ( Exception e )
        {
            LOG.error("Problem Loading Database Configurations For: $sql", e)
        }

        return tableMap
    }
}
