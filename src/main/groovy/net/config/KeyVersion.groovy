package net.config

/**
 * Builds a map with a versioned key if necessary and updates
 * _keyVersion. This should safely allow the following usage:
 *
 * It keeps a current count of all the "similar" keys encountered
 * so that a new key may be generated with a version appended to
 * ensure uniqueness.
 *
 * foo.bar
 * foo.bar.1
 * foo.bar.2
 *
 * This is not thread safe and an instance should not be shared.
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

    private def keyVersionCount = new HashMap<String,Integer>()

    def updateMapWithKeyValue(Map<String,String> originalMap, String key, String value) {

        if ( key == null || value == null )
        {
            return
        }

        if ( keyVersionCount.containsKey(key) )
        {
            def indexedKey = buildIndexedKeyAndUpdateKeyCount(key)
            originalMap.put(indexedKey, value)
        }
        else
        {
            originalMap.put(key, value)
        }
    }


    def updateMapWithKeyValues(Map<String,String> originalMap, Map<String,String> additionalMap) {

        additionalMap.entrySet().each { entry ->

            if ( originalMap.containsKey(entry.key) )
            {
                def indexedKey = buildIndexedKeyAndUpdateKeyCount(entry.key)
                originalMap.put(indexedKey, entry.value)
            }
            else
            {
                originalMap.put(entry.key, entry.value)
            }
        }
    }

    def buildMapFromOriginal(original, additional) {

        def combinedMap = new HashMap()
        newMap.putAll(original)
        updateMapWithKeyValues(combinedMap, additional)

        return combinedMap
    }


    private def String buildIndexedKeyAndUpdateKeyCount(String key) {

        def indexedKey = key

        if ( keyVersionCount.containsKey(key) )
        {
            def keyIndex = keyVersionCount.get(key) + 1
            indexedKey = key + "." + keyIndex
            keyVersionCount.put(key, keyIndex)
        }
        else
        {
            indexedKey = key + "." + 1
            keyVersionCount.put(key, 1)
        }

        return indexedKey
    }
}
