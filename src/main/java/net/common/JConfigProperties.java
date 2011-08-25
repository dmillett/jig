package net.common;

/**
 * A common point across JVM languages to document system properties
 * used by jConfigMap. These properties can be set as JVM args.
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
public enum JConfigProperties {

    /** System Property for local configuration file(s) location outside of 'classpath/config' */
    JCONFIG_LOCATION("jConfigMap.location"),
    /** A remote URL based location with config files */
    JCONFIG_URL_LOCATION("jConfigMap.url"),
    /** Global overrides entered as JVM args. Ex: jConfigMap.entry.foo=bar */
    JCONFIG_COMMAND_LINE_PROP("jConfigMap.entry"),
    /** A config file suffix to support environment specific configurations  */
    JCONFIG_FILE_ENVIRONMENT("jConfigMap.file.env")
    ;

    private final String _systemPropertyName;

    JConfigProperties(String systemPropertyName) {
        _systemPropertyName = systemPropertyName;
    }

    public String getName() {
        return _systemPropertyName;
    }
}
