package net.config

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

        GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "ConfigOne.xml"
    }

    // Load a specific test config file
    void test__loadFromXmlFile_test_location() {

        def testConfigFile = GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "ConfigOne.xml"
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
        assertEquals(85, configMap.size())
    }

    // Load a two deep map with filename as the first level
    void test__loadMapsFromFiles() {

        def configLoader = new ConfigLoader()
        def configMaps = configLoader.loadMapsFromFiles()

        assertNotNull(configMaps)
        assertFalse(configMaps.isEmpty())
        assertEquals(7, configMaps.size())
    }

    void test__loadConfigFilesFromClasspath() {

        def configLoader = new ConfigLoader()
        List<String> classpathFiles = configLoader.loadConfigFilesFromClasspath()

        assertEquals(1, classpathFiles.size())
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
        assertEquals(85, configMap.size())
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
}
