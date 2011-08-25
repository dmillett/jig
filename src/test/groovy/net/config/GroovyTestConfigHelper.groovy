package net.config

import net.common.JConfigProperties

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

        if ( System.getProperty(JConfigProperties.JCONFIG_LOCATION.getName()) == null )
        {
            def testConfigPath = new File('').absolutePath + "/src/test/resources/config/"
            System.setProperty(JConfigProperties.JCONFIG_LOCATION.getName(), testConfigPath)
        }

        return System.getProperty(JConfigProperties.JCONFIG_LOCATION.getName())
    }

    def static updateSystemPropertiesWithConfigEnv(String envIndicator) {

        if ( envIndicator != null )
        {
            System.setProperty(JConfigProperties.JCONFIG_FILE_ENVIRONMENT.getName(), envIndicator)
        }
    }
}
