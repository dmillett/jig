package net.config

import groovy.sql.Sql

/**
 * Intellij and possibly Eclipse do not compile and move
 * directories in the same manner as Gradle. Intellij will dump all the
 * test config files in with the non-test files.
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
class ConfigLoaderTest
    extends GroovyTestCase {

    @Override
    protected void setUp() {
        GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "/ConfigOne.xml"
    }

    // Load a specific test config file
    void test__loadFromXmlFile_test_location() {

        def testConfigFile = GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "/ConfigOne.xml"
        def configLoader = new ConfigLoader()

        def configMap = configLoader.loadFromXmlFile(testConfigFile)
        assertNotNull(configMap)
        assertFalse(configMap.isEmpty())
        assertEquals(19, configMap.size())
    }

    // List all of the config files to load
    void test__loadConfigFiles() {

        def configLoader = new ConfigLoader()
        def configFileNames = configLoader.loadConfigFilesFromOverride()

        assertNotNull(configFileNames)
        assertEquals(10, configFileNames.size())
    }

    // Load all the test configs into a single depth map
    void test__loadFromFiles() {

        def configLoader = new ConfigLoader()
        def configMap = configLoader.loadFromFiles()

        assertNotNull(configMap)
        assertFalse(configMap.isEmpty())
        assertEquals(80, configMap.size())
    }

    // Load a two deep map with filename as the first level
    void test__loadMapsFromFiles() {

        // Have to mock this out to some extent. The DB retrieval is tested in SqlRetrieverTest
        //buildAndPopulateDatabase()

        def configLoader = new ConfigLoader()
        def configMaps = configLoader.loadMapsFromFiles()

        assertNotNull(configMaps)
        assertFalse(configMaps.isEmpty())

        // Note that the entry and values for DatabaseConfig.xml were removed
        assertEquals(6, configMaps.size())
        assertTrue(configMaps.containsKey("ConfigOne.xml"))
        assertTrue(configMaps.containsKey("ExampleConfig.xml"))
        assertTrue(configMaps.containsKey("JsonExampleOne.json"))
        assertTrue(configMaps.containsKey("JsonExampleTwo.json"))
        assertTrue(configMaps.containsKey("EnvironmentConfig_dev.xml"))
    }

    void test__loadConfigFilesFromClasspath() {

        def configLoader = new ConfigLoader()
        List<String> classpathFiles = configLoader.loadConfigFilesFromClasspath()

        /*
        It looks like Gradle now dumps this at 'build/resources/main/config'
        instead of 'classes/main/resources/config'
        It used to pick up 'TemplateConfig.xml'
        */
        assertEquals(0, classpathFiles.size())
    }

//    void test__loadFromCommandLineSystemProperties() {
//
//        GroovyTestConfigHelper.addSystemPropertyWithSpecificValue("foo", "2")
//        GroovyTestConfigHelper.addSystemPropertyWithSpecificValue("bar", "false")
//
//        def configLoader = new ConfigLoader()
//        def commandLineOverrides = configLoader.loadFromCommandLineSystemProperties();
//
//        assertNotNull(commandLineOverrides)
//        assertEquals(2, commandLineOverrides.size())
//
//        GroovyTestConfigHelper.removeSystemPropertyWithSpecificValue("foo")
//        GroovyTestConfigHelper.removeSystemPropertyWithSpecificValue("bar")
//    }

    void test__loadFromCommandLineSystemProperties_override() {

        def testKey = "key.two.int"
        def originalValue = "1"
        GroovyTestConfigHelper.addSystemPropertyWithSpecificValue(testKey, "42")

        // Overriding a value in ConfigOne.xml
        def configLoader = new ConfigLoader()
        def configMap = configLoader.loadFromFiles()

        assertNotNull(configMap)
        assertEquals(80, configMap.size())
        assertEquals("42", configMap.get(testKey))

        // Cleanup
        GroovyTestConfigHelper.removeSystemPropertyWithSpecificValue(testKey)
        configMap.put(testKey, originalValue)
        assertEquals("1", configMap.get(testKey))
    }

    void test__loadConfigFilesForEnvironment() {

        GroovyTestConfigHelper.updateSystemPropertiesWithConfigEnv("Dev")
        def configLoader = new ConfigLoader()
        def configMap = configLoader.loadFromFiles()

        assertNotNull(configMap)
        assertEquals(2, configMap.size())

        GroovyTestConfigHelper.updateSystemPropertiesWithConfigEnv("")
    }

    def Sql buildAndPopulateDatabase() {

        // See DatabaseConfig.xml in test (it should correspond to these values.
        def driver = "org.h2.Driver"
        def url = "jdbc:h2:mem:"
        def user = "testUser"
        def password = "password"
        def table = "SQLDBEXAMPLE"

        def sql2 = Sql.newInstance(url, user, password, driver)
        sql2.execute("create table SQLDBEXAMPLE (id int primary key, key varchar(50), value varchar(50))")
        sql2.execute("insert into SQLDBEXAMPLE values (1, 'db.one', '1'), (2, 'db.two', 'two'), (3, 'db.three', 'false')")

        def insertedRows = sql2.rows("SELECT * FROM SQLDBEXAMPLE")
        assertEquals(3, insertedRows.size())

        return sql2
    }
}
