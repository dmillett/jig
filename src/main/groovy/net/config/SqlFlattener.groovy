package net.config

import groovy.sql.Sql
import net.config.client.ConfigLookup
import org.apache.log4j.Logger
import java.util.Map.Entry

/**
 * Loads from multiple configuration only databases given the following:
 *
 * 1) database url
 * 2) database user name
 * 3) database password (unencrypted)
 * 4) database driver class
 * 5) database table
 *
 * <pre>
 * <dbConfigTable name="bar">
 *   <tableName>Foo</tableName>
 *   <userName>someUser</userName>
 *   <userPassword>unencryptedPassword</userPassword>
 *   <url>some://url/with/db/driver</url>
 *   <driver>someJdbcDriver</driver>
 * </dbConfigTable>
 * </pre>
 *
 * Already parsed and flattened into something like this:
 *
 * structures.dbconfigtable.name.bar.tablename : Foo
 * structures.dbconfigtable.name.bar.username : someUser
 * structures.dbconfigtable.name.bar.userpassword : unecryptedPassword
 * structures.dbconfigtable.name.bar.url : some://url/with/db/driver
 * structures.dbconfigtable.name.bar.driver : someJdbcDriver
 *
 * Notes:
 * These databases should be stand alone configuration databases that do not
 * contain sensitive information. Isolating them from the rest of persistent
 * data will make it easier for developers and business operations people to
 * update stored values. It will also reduce the overhead and influence of
 * company/dba selected databases for use.
 *
 * Parameters for DB tables with Customer information can certainly be listed
 * in the configuration files, sql tables, etc. However, they should have
 * previously encrypted values and rely upon a separate Client application
 * to decrypt/connect to the database with PCI information.
 *
 * Removes values from config map after generating key-value pairs
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
class SqlFlattener {

    private static final def LOG = Logger.getLogger(SqlFlattener.class)

    public static final def DB_TABLE_PARAMS = ~/dbconfigtable.*/
    public static final def TABLE = "tablename"
    public static final def USER = "username"
    public static final def PASSWORD = "userpassword"
    public static final def DB_URL = "url"
    public static final def DRIVER = "driver"

    /**
     * Loop through previously loaded configurations looking for any entries
     * related to database configurations. See DB_TABLE_PARAMS
     *
     * @param configs
     * @return
     */
    def Map<String, Map<String, String>> buildMapFromDatabaseTables(Map<String, Map<String, String>> configs) {

        // Config map with all db config table params
        def dbParams = findDbConfigParams(configs)
        def dbTableConfigs = groupDbConfigParamsByTable(dbParams)

        return dbTableConfigs
    }

    /**
     * Not sure I like the code herein that groups -- sigh
     *
     * Responsible for looking up database configuration key:values and storing all
     * those key:values per table in resulting Map.
     *
     * @param dbConfigParams
     * @return
     */
    protected def Map<String, Map<String, String>> groupDbConfigParamsByTable(Map<String, String> dbConfigParams) {

        // Sorting on natural order should be enough
        def groupedMap = new TreeMap(dbConfigParams)
        def allDbConfigs = new HashMap<String, Map<String, String>>()
        def validDbConfigParams = validDbConfigParams(groupedMap)

        if ( !validDbConfigParams )
        {
            LOG.warn("Cannot Load Database Configurations Due To Invalid Connection Parameters")
            return allDbConfigs
        }

        def index = 0
        def increment = 5
        def size = groupedMap.size();
        def sqlRetriever = new SqlRetriever()

        while ( index < size )
        {
            def end = index + increment
            def subMap = extractSubGroup(groupedMap.entrySet(), index, increment)
            def tableName = extractValue(subMap, TABLE)

            def sql = createSqlReader(subMap)
            def dbConfigs = sqlRetriever.loadFromDatabaseWithSelect(tableName, sql)
            if ( sql != null ) { sql.close() }

            for ( entry in dbConfigs )
            {
                allDbConfigs.put(entry.key, entry.value)
            }

            index += increment
        }

        return allDbConfigs
    }

    /**
     *
     * @param entries
     * @param currentIndex
     * @param increment
     * @return
     */
    protected def Map<String, String> extractSubGroup(Set<Entry> entries, int currentIndex, int increment) {

        def subMap = new HashMap<String, String>(increment)
        def i = 0

        for ( entry in entries )
        {
            if ( i == increment )
            {
                break
            }

            subMap.put(entry.key, entry.value)
            i++
        }

        return subMap
    }

    protected def Sql createSqlReader(Map<String, String> sqlTableInfos) {

        def user = extractValue(sqlTableInfos, USER)
        def password = extractValue(sqlTableInfos, PASSWORD)
        def url = extractValue(sqlTableInfos, DB_URL)
        def driver = extractValue(sqlTableInfos, DRIVER)

        return Sql.newInstance(url, user, password, driver)
    }

    /**
     * Pull a specific value for a dbconfig param from a sub-group table.
     * A subgroup consists of 5 values. See "loadFromDatabaseWithSelect()"
     *
     * @param subMap The five db config params needed to read a sql DB table
     * @param target The value to retrieve (url, user, etc)
     * @return The corresponding value for 'target' or 'null'
     */
    protected def String extractValue(Map<String, String> subMap, String target) {

        if ( subMap == null || target == null || target.empty )
        {
            LOG.warn("Cannot Retrieve Database Connection Parameter $target")
            return null
        }

        for ( entry in subMap )
        {
            if ( entry.key.endsWith(target.toLowerCase()) )
            {
                return entry.value
            }
        }

        return null
    }

    /**
     * There should be 5 entries for each database configuration table to load configs from.
     * This relies on db config params extracted from the overall config map.
     * See "findDbConfigParameters()".  This also ensures that if there are
     * 5 table configs present, then there will be 5 keys for the params below.
     *
     * 1) tablename
     * 2) username
     * 3) userpassword
     * 4) url
     * 5) driver
     *
     * @param dbConfigParams
     * @return true if the modulus of 'dbConfigParams % 5' is 0, otherwise false
     */
    protected def boolean validDbConfigParams(Map<String, String> dbConfigParams) {

        if ( dbConfigParams == null || dbConfigParams.empty )
        {
            LOG.warn("Invalid DB Configs. Five Values Are Required For Each DB Connection (see constants)")
            return false
        }

        def modulus = dbConfigParams.size() % 5
        if ( modulus != 0 )
        {
            LOG.warn("Incorrect Number Of Config Table Params. 5 Are Required")
            return false
        }

        def dbTableCount = dbConfigParams.size() / 5
        def helper = new ConfigLookup()

        def counts = new HashMap<String, Integer>(5)
        counts.put(TABLE, helper.findByKeyPattern(dbConfigParams, helper.buildPattern(TABLE)).size())
        counts.put(USER, helper.findByKeyPattern(dbConfigParams, helper.buildPattern(USER)).size())
        counts.put(PASSWORD, helper.findByKeyPattern(dbConfigParams, helper.buildPattern(PASSWORD)).size())
        counts.put(DB_URL, helper.findByKeyPattern(dbConfigParams, helper.buildPattern(DB_URL)).size())
        counts.put(DRIVER, helper.findByKeyPattern(dbConfigParams, helper.buildPattern(DRIVER)).size())

        for ( entry in counts )
        {
            if ( entry.value != dbTableCount )
            {
                LOG.warn("Incorrect Number Of DB Connection Params For ${entry.key}")
                return false;
            }
        }

        true
    }

    /**
     * Remove the db config parameters as they should not be used in a client application. If the Client
     * requires app specific databases, then those should be separate from the config database. That means
     * they should not have the dbConfigTable
     *
     * @param allConfigs
     * @param dbParams
     * @return
     */
    def Map<String, String> purgeDbConfigParameters(Map<String, String> allConfigs, Map<String,String> dbParams) {

        def dbcleanedConfig = new HashMap<String, String>()

        for ( entry in allConfigs )
        {
            if ( !dbParams.containsKey(entry.key) )
            {
                dbcleanedConfig.put(entry.key, entry.value)
            }
        }

        return dbcleanedConfig
    }

    /**
     * Loop through each config file and check its config entries for database
     * table connection parameters.
     *
     * @param configMap All of the loaded file/url configs.
     * @return All database connection parameters for configs in the database
     */
    def Map<String, String> findDbConfigParams(Map<String, Map<String, String>> configMap) {

        def dbConfigTableParams = new HashMap<String, String>();
        def cfgLookup = new ConfigLookup()

        for ( configFileName in configMap.keySet() )
        {
            def dbTableParams = cfgLookup.findByKeyPattern(configMap.get(configFileName), DB_TABLE_PARAMS)
            dbConfigTableParams.putAll(dbTableParams)
        }

        return dbConfigTableParams
    }
}
