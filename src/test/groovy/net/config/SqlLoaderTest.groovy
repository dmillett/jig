package net.config

import groovy.sql.Sql

/**
 *
 */
class SqlLoaderTest
    extends GroovyTestCase {

    def sql

    @Override
    protected void setUp() {

        sql = Sql.newInstance("jdbc:h2:mem:", "test", "", "org.h2.Driver")
        sql.execute("create table CONFIG (id int primary key, key varchar(50), value varchar(50))")
        sql.execute("insert into CONFIG values (1, 'db.one', '1'), (2, 'db.two', 'two'), (3, 'db.three', 'false')")

        def insertedRows = sql.rows("SELECT * FROM CONFIG")
        assertEquals(3, insertedRows.size())
    }

    @Override
    protected void tearDown() {
        sql.close()
    }

    void test__findDbConfigParams_fails() {

        def dbParams = buildInvalidDbConfigParams()

        def configFileMap = new HashMap<String, Map<String,String>>();
        configFileMap.put("Test-File.tst", dbParams)

        def sqlLoader = new SqlLoader()
        def dbConfigParams = sqlLoader.findDbConfigParams(configFileMap)

        assertEquals(4, dbConfigParams.size())
    }

    void test__findDbConfigParams_simple() {

        def dbParams = buildValidDbConfigParams()
        def configFileMap = new HashMap<String, Map<String,String>>();
        configFileMap.put("Test-File.tst", dbParams)

        def sqlLoader = new SqlLoader()
        def dbConfigParams = sqlLoader.findDbConfigParams(configFileMap)

        assertEquals(5, dbConfigParams.size())
    }

    void test__validDbConfigParams_invalid() {

        def dbParms = buildInvalidDbConfigParams()
        def sqlLoader = new SqlLoader()

        assertFalse(sqlLoader.validDbConfigParams(dbParms))
    }

    void test__validDbConfigParams_valid() {

        def dbParams = buildValidDbConfigParams()

        def sqlLoader = new SqlLoader()
        def isValid = sqlLoader.validDbConfigParams(dbParams)
        assertTrue(isValid)
    }

    void test__extractValue() {

        def valid = buildValidDbConfigParams()
        def sqlLoader = new SqlLoader()

        assertNull(sqlLoader.extractValue(valid, "Fail"))

        assertEquals("Foo", sqlLoader.extractValue(valid, SqlLoader.TABLE))
        assertEquals("someUser", sqlLoader.extractValue(valid, SqlLoader.USER))
        assertEquals("unencryptedPassword", sqlLoader.extractValue(valid, SqlLoader.PASSWORD))
        assertEquals("some://url/with/db/driver", sqlLoader.extractValue(valid, SqlLoader.DB_URL))
        assertEquals("someJdbcDriver", sqlLoader.extractValue(valid, SqlLoader.DRIVER))
    }

    void test__loadFromDatabase() {

        def dbParams = new HashMap<String, String>()
        dbParams.put("dbconfigtable.name.bar.tablename", "config")
        dbParams.put("dbconfigtable.name.bar.username", "test")
        dbParams.put("dbconfigtable.name.bar.userpassword", "")
        dbParams.put("dbconfigtable.name.bar.url", "jdbc:h2:mem:")
        dbParams.put("dbconfigtable.name.bar.driver", "org.h2.Driver")

        def sqlLoader = new SqlLoader()
        def result = sqlLoader.loadFromDatabase("config", sql)

        // 3 config key:values for 1 table
        assertEquals(1, result.size())
        assertEquals(3, result.entrySet().iterator().next().value.size())
    }


    void test__groupDbConfigParamsByTable_mocked() {

        def dbParams = buildValidDbConfigParams2()
        def sqlLoader = new SqlLoader()

        // todo: mock sqlLoader.loadFromDatabase
        sqlLoader.metaClass.loadFromDatabase = ["mock.db.config":"mocked value"]
        def dbConfigs = sqlLoader.groupDbConfigParamsByTable(dbParams)

    }

    private def Map<String, String> buildInvalidDbConfigParams() {

        def dbParams = buildValidDbConfigParams()
        dbParams.remove("dbconfigtable.name.bar.driver")
        dbParams.put("foo.bar", "zoo")

        return dbParams
    }

    private def Map<String, String> buildValidDbConfigParams() {

        def dbParams = new HashMap<String, String>()
        dbParams.put("dbconfigtable.name.bar.tablename", "Foo")
        dbParams.put("dbconfigtable.name.bar.username", "someUser")
        dbParams.put("dbconfigtable.name.bar.userpassword", "unencryptedPassword")
        dbParams.put("dbconfigtable.name.bar.url", "some://url/with/db/driver")
        dbParams.put("dbconfigtable.name.bar.driver", "someJdbcDriver")

        return dbParams
    }

    private def Map<String, String> buildValidDbConfigParams2() {

        def dbParams = new HashMap<String, String>()
        dbParams.put("dbconfigtable.name.bar.tablename", "Foo")
        dbParams.put("dbconfigtable.name.bar22.tablename", "Foo22")

        dbParams.put("dbconfigtable.name.bar.username", "someUser")
        dbParams.put("dbconfigtable.name.bar22.username", "someUser22")

        dbParams.put("dbconfigtable.name.bar.userpassword", "unencryptedPassword")
        dbParams.put("dbconfigtable.name.bar22.userpassword", "unencryptedPassword22")

        dbParams.put("dbconfigtable.name.bar.url", "some://url/with/db/driver")
        dbParams.put("dbconfigtable.name.bar22.url", "some://url/with/db/driver22")

        dbParams.put("dbconfigtable.name.bar.driver", "someJdbcDriver")
        dbParams.put("dbconfigtable.name.bar22.driver", "someJdbcDriver22")

        return dbParams
    }
}
