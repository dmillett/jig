package net.config

import groovy.sql.Sql
import net.config.client.ConfigLookup
import org.apache.log4j.Logger

/**
 *
 * Loads from multiple databases given the following:
 * 1) database url
 * 2) database user name
 * 3) database password (unencrypted)
 * 4) database driver class
 * 5) database table
 *
 * Notes:
 * These databases should be stand alone configuration databases that do not
 * contain sensitive information. Isolating them from the rest of persistent
 * data will make it easier for developers and business operations people to
 * update stored values. It will also reduce the overhead and influence of
 * company/dba selected databases for use.
 *
 * Removes values from config map after generating key-value pairs
 *
 */
class SqlLoader {

    private static final def DB_TABLE_PARAMS = ~/dbConfigTable.*/
    private static final def LOG = Logger.getLogger(SqlLoader.class)

    def buildMapFromDatabaseTables(Map<String, Map<String, String>> configs) {



    }

    /**
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
     * dbConfigTable.bar.tableName : Foo
     * dbConfigTable.bar.userName : someUser
     * dbConfigTable.bar.userPassword : unecryptedPassword
     * dbConfigTable.bar.url : some://url/with/db/driver
     * dbConfigTable.bar.driver : someJdbcDriver
     *
     * Look up all the key-value property style configs for each row in the
     * database listed above and load into a map.
     *
     * @param sqlTableInfos
     * @return
     */
    def Map<String, String> load(Map<String, String> sqlTableInfos) {


//        def sql = Sql.newInstance("jdbc:mysql://localhost:3306/mydb", "user",
//                              "pswd", "com.mysql.jdbc.Driver")
//
//        sql.eachRow("select * from FOOD where type=${foo}") {
//            println "Gromit likes ${it.name}"
//          }

        return null
    }

    /**
     * url
     * username
     * password
     * driver class
     *
     * @return
     */
    def Sql buildSqlFromConfig() {
    }


    def Map<String, String> purgeDbConfigParameters(Map<String, String> allConfigs, Map<String,String> dbParams) {

        def dbcleanedConfig = new HashMap<String, String>()

        allConfigs.entrySet().each { entry ->
            if ( !dbParams.containsKey(entry.key) )
            {
                dbcleanedConfig.put(entry.key, entry.value)
            }
        }

        return dbcleanedConfig
    }

    def Map<String, Map<String, String>> purgeDbTableParams(Map<String, Map<String, String>> configs) {



    }


    /**
     * Loop through each config file and check its config entries for database
     * table connection parameters.
     *
     * @param configMap All of the loaded file/url configs.
     * @return All database connection parameters for configs in the database
     */
    def Map<String, Map<String, String>> findDbConfigParams(Map<String, Map<String, String>> configMap) {

        def dbConfigTableParams = new HashMap<String, String>();
        def cfgLookup = new ConfigLookup()

        configMap.keySet().each { configFileName ->

            def dbTableParams = cfgLookup.findByKeyPattern(configMap.get(configFileName), DB_TABLE_PARAMS)
            dbConfigTableParams.putAll(dbTableParams)
        }

        return dbConfigTableParams
    }
}
