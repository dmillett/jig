package net.config

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
class ConfigLoaderTest
    extends GroovyTestCase {

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
        assertEquals(4, configFileNames.size())
    }

    // Load all the test configs into a single depth map
    void test__loadFromXmlFiles() {

        def configLoader = new ConfigLoader()
        def configMap = configLoader.loadFromXmlFiles()

        assertNotNull(configMap)
        assertFalse(configMap.isEmpty())
        assertEquals(39, configMap.size())
    }

    // Load a two deep map with filename as the first level
    void test__loadMapsFromFiles() {

        def configLoader = new ConfigLoader()
        def configMaps = configLoader.loadMapsFromFiles()

        assertNotNull(configMaps)
        assertFalse(configMaps.isEmpty())
        assertEquals(3, configMaps.size())
    }

    void test__loadConfigFilesFromClasspath() {

        def configLoader = new ConfigLoader()
        configLoader.loadConfigFilesFromClasspath()
    }
}
