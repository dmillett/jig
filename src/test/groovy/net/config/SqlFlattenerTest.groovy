package net.config

import groovy.mock.interceptor.MockFor

/**
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
class SqlFlattenerTest
    extends GroovyTestCase {

    void test__findDbConfigParams_fails() {

        def dbParams = buildInvalidDbConfigParams()

        def configFileMap = new HashMap<String, Map<String,String>>();
        configFileMap.put("Test-File.tst", dbParams)

        def sqlLoader = new SqlFlattener()
        def dbConfigParams = sqlLoader.findDbConfigParams(configFileMap)

        assertEquals(4, dbConfigParams.size())
    }

    void test__findDbConfigParams_simple() {

        def dbParams = buildValidDbConfigParams()
        def configFileMap = new HashMap<String, Map<String,String>>();
        configFileMap.put("Test-File.tst", dbParams)

        def sqlLoader = new SqlFlattener()
        def dbConfigParams = sqlLoader.findDbConfigParams(configFileMap)

        assertEquals(5, dbConfigParams.size())
    }

    void test__validDbConfigParams_invalid() {

        def dbParms = buildInvalidDbConfigParams()
        def sqlLoader = new SqlFlattener()

        assertFalse(sqlLoader.validDbConfigParams(dbParms))
    }

    void test__validDbConfigParams_valid() {

        def dbParams = buildValidDbConfigParams()

        def sqlLoader = new SqlFlattener()
        def isValid = sqlLoader.validDbConfigParams(dbParams)
        assertTrue(isValid)
    }

    void test__extractValue() {

        def valid = buildValidDbConfigParams()
        def sqlLoader = new SqlFlattener()

        assertNull(sqlLoader.extractValue(valid, "Fail"))

        assertEquals("Foo", sqlLoader.extractValue(valid, SqlFlattener.TABLE))
        assertEquals("someUser", sqlLoader.extractValue(valid, SqlFlattener.USER))
        assertEquals("unencryptedPassword", sqlLoader.extractValue(valid, SqlFlattener.PASSWORD))
        assertEquals("some://url/with/db/driver", sqlLoader.extractValue(valid, SqlFlattener.DB_URL))
        assertEquals("someJdbcDriver", sqlLoader.extractValue(valid, SqlFlattener.DRIVER))
    }

    void test__groupDbConfigParamsByTable_mocked() {

        def tableName = "mock-table"
        def mockData = mockData(tableName)
        def mock = new MockFor(SqlRetriever)

        // Ignore the two parameters (a, b)
        mock.demand.loadFromDatabaseWithSelect(1) { a, b -> return mockData }

        mock.use {
            def dbParams = buildInMemoryDbParams()
            def sqlFlattener = new SqlFlattener()
            def Map<String, Map<String, String>> groupedMap = sqlFlattener.groupDbConfigParamsByTable(dbParams)

            assertEquals(1, groupedMap.size())
            assertTrue(groupedMap.containsKey(tableName))
            assertEquals(2, groupedMap.get(tableName).size())
        }
    }

    void test__extractSubGroup() {

        def map = new TreeMap(buildValidDbConfigParams2())
        def sqlLoader = new SqlFlattener()

        def subMap = sqlLoader.extractSubGroup(map.entrySet(), 0, 5)

        assertTrue(subMap.containsKey("dbconfigtable.name.bar.tablename"))
        assertTrue(subMap.containsKey("dbconfigtable.name.bar.username"))
        assertTrue(subMap.containsKey("dbconfigtable.name.bar.userpassword"))
        assertTrue(subMap.containsKey("dbconfigtable.name.bar.url"))
        assertTrue(subMap.containsKey("dbconfigtable.name.bar.driver"))
    }


    private def Map<String, Map<String, String>> mockData(String tableName) {

        def tableData = new HashMap<String, String>()
        tableData.put("property.one", "one")
        tableData.put("property.two", "two")

        def tableConfigData = new HashMap<String, Map<String, String>>()
        tableConfigData.put(tableName, tableData)

        return tableConfigData
    }

    private def Map<String, String> buildInMemoryDbParams() {

        def dbParams = new HashMap<String, String>()
        dbParams.put("dbconfigtable.name.bar.tablename", "config")
        dbParams.put("dbconfigtable.name.bar.username", "test")
        dbParams.put("dbconfigtable.name.bar.userpassword", "")
        dbParams.put("dbconfigtable.name.bar.url", "jdbc:h2:mem:")
        dbParams.put("dbconfigtable.name.bar.driver", "org.h2.Driver")

        return dbParams
    }

    private def Map<String, String> buildInvalidDbConfigParams() {

        def dbParams = buildValidDbConfigParams()
        dbParams.remove("dbconfigtable.name.bar.driver")
        dbParams.put("foo.bar", "zoo")

        return dbParams
    }

    private def Map<String, String> buildOrderedValidDbConfigParams() {
        return new TreeMap<String, String>(buildValidDbConfigParams())
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
