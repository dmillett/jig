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
public enum JigProperties {

    /** System Property for local configuration file(s) location outside of 'classpath/config' */
    JIG_LOCATION("jig.location"),
    /** A remote URL based location with config files */
    JIG_URL_LOCATION("jig.url"),
    /** Global overrides entered as JVM args. Ex: jig.entry.foo=bar */
    JIG_COMMAND_LINE_PROP("jig.entry"),
    /** A config file suffix to support environment specific configurations  */
    JIG_FILE_ENVIRONMENT("jig.file.env")
    ;

    private final String _systemPropertyName;

    JigProperties(String systemPropertyName) {
        _systemPropertyName = systemPropertyName;
    }

    public String getName() {
        return _systemPropertyName;
    }
}
