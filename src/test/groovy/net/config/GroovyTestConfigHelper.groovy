package net.config

import net.common.JigProperties

/**
 * Useful for setting config locations for individual unit tests.
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
class GroovyTestConfigHelper {

    def static String updateSystemPropertyConfigLocation() {

        if ( System.getProperty(JigProperties.JIG_LOCATION.getName()) == null )
        {
            def testConfigPath = new File('').absolutePath + "/src/test/resources/config/"
            System.setProperty(JigProperties.JIG_LOCATION.getName(), testConfigPath)
        }

        return System.getProperty(JigProperties.JIG_LOCATION.getName())
    }

    def static updateSystemPropertiesWithConfigEnv(String envIndicator) {

        if ( envIndicator != null )
        {
            System.setProperty(JigProperties.JIG_FILE_ENVIRONMENT.getName(), envIndicator)
        }
    }

    def static addSystemPropertyWithSpecificValue(String key, String value) {

        if ( key != null )
        {
            def configKey = buildOverrideKey(key)
            System.setProperty(configKey, value)
        }
    }

    def static removeSystemPropertyWithSpecificValue(String key) {

        if ( key != null )
        {
            def configKey = buildOverrideKey(key)
            System.getProperties().remove(configKey)
        }
    }

    private static def String buildOverrideKey(String key) {

        if ( key != null )
        {
            return JigProperties.JIG_COMMAND_LINE_PROP.getName() + "." + key
        }

        return null;
    }
}
