package net.config

/**
 * Builds a map with a versioned key if necessary and updates
 * _keyVersion. This should safely allow the following usage:
 *
 * someMap.putAll(keyVersion.getKeyValues(....))
 * keyVersion = keyVersion.getKeyCount
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
class KeyVersion {

    def _keyVersion = 1

    def Map<String,String> getKeyValues(keyCount, key, value, currentKeyValues) {

        def keyValues = new HashMap<String,String>()
        _keyVersion = keyCount

        if ( key == null )
        {
            return keyValues
        }

        if ( currentKeyValues.containsKey(key) )
        {
            def versionedKey = key + "." + keyCount
            keyValues.put(versionedKey, value)
            _keyVersion++
        }
        else
        {
            keyValues.put(key, value)
        }

        return keyValues
    }

    def Map<String,String> getKeyValues(keyVersion, originalKeyValues, additionalKeyValues) {

        def keyValues = new HashMap<String,String>()

        if ( additionalKeyValues == null || additionalKeyValues.empty )
        {
            return keyValues
        }

        _keyVersion = keyVersion
        additionalKeyValues.each { entry ->
            keyValues.putAll(getKeyValues(_keyVersion, entry.key, entry.value, originalKeyValues))
        }

        return keyValues
    }
}
