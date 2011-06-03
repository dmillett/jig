package net.config

import groovy.json.JsonSlurper
import org.apache.log4j.Logger

/**
 * This uses Groovy's JSON tranformer and flattens the structure
 * into a key-value pair similar to XmlFlattener.  Maps and Arrays
 * 'keys' are flattened to represent the path their corresponding value.
 *
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
class JsonFlattener {

    private static final def Logger LOG = Logger.getLogger(JsonFlattener.class)
    private final def KeyVersion _keyVersion = new KeyVersion()

    /**
     * For Groovy JsonSlurper to work properly, it will only take the file
     * in URL format. If it's a local file, then "file:///foo/bar/file.json"
     *
     * @param jsonFileName
     * @return
     */
    def Map<String,String> flatten(String jsonFileName) {

        def flattenedKeyValues

        try
        {
            def jsSlurper = new JsonSlurper()
            def parsedFile = jsSlurper.parseText(jsonFileName.toURL().text)
            flattenedKeyValues = flattenGroovyJsonObject(parsedFile)
        }
        catch ( Exception e )
        {
            LOG.error("Could Not Load JSON Configuration File")
            flattenedKeyValues = new HashMap<String,String>()
        }

        return flattenedKeyValues
    }

    private def isValidUrlFile(urlFile) {

        try
        {
            new URL(urlFile)
            return true
        }
        catch ( Exception e )
        {
            LOG.error("Cannot Parse JSON Config From Non URL File", e)
            return false;
        }
    }

    /**
     * Groovy transforms JSON to either a Map or List based on the root node.
     *
     * @param groovyJsonObject
     * @return A Map of String,String
     */
    def Map<String,String> flattenGroovyJsonObject(groovyJsonObject) {

        def keyValues = new HashMap<String,String>()

        if ( groovyJsonObject == null )
        {
            return keyValues
        }

        if ( groovyJsonObject instanceof Map )
        {
            keyValues.putAll(transformGroovyJsonMap(groovyJsonObject, ""))
        }
        else if ( groovyJsonObject instanceof List )
        {
            keyValues.putAll(transformJsonArray(groovyJsonObject, ""))
        }
        else
        {
            // todo "foo": "bar"
        }

        return keyValues
    }

    /**
     *
     * @param jsonMap
     * @param currentName
     * @return
     */
    def Map<String,String> transformGroovyJsonMap(Map jsonMap, String currentName) {

        if ( jsonMap == null || jsonMap.isEmpty() )
        {
            return new HashMap<String,String>()
        }

        def keyValues = new HashMap<String,String>()

        jsonMap.each { entry ->

            def key = entry.key
            if ( currentName != null && !currentName.empty )
            {
                key = currentName + "." + key
            }

            if ( entry == null || entry.value == null )
            {
                println("Null Entry Or Entry Value")
            }
            else if ( entry.value instanceof List )
            {
                def jsonListKeyValues = transformJsonArray(entry.value, key)
                keyValues.putAll(jsonListKeyValues)
            }
            else if ( entry.value instanceof Map)
            {
                def jsonMapKeyValues = transformGroovyJsonMap(entry.value, key)
                keyValues.putAll(jsonMapKeyValues)
            }
            else
            {
                def value = String.valueOf(entry.value)
                _keyVersion.updateMapWithKeyValue(keyValues, key, value)
            }
        }

        return keyValues
    }

    /**
     * Flatten Groovy-JSON Array objects
     *
     * @param jsonArray
     * @param currentName
     * @return A map of String,String
     */
    def Map<String,String> transformJsonArray(List jsonArray, String currentName) {

        if ( jsonArray == null || jsonArray.empty )
        {
            return new HashMap<String, String>()
        }

        def keyValues = new HashMap<String,String>()

        jsonArray.each { jsonElement ->

            if ( jsonElement == null )
            {
                keyValues.put(currentName, null)
            }
            else if ( jsonElement instanceof Map)
            {
                def jsonMapKeyValues = transformGroovyJsonMap(jsonElement, currentName)
                _keyVersion.updateMapWithKeyValues(keyValues, jsonMapKeyValues)
            }
            else if ( jsonElement instanceof List )
            {
                def jsonArrayKeyValues = transformJsonArray(jsonElement, currentName)
                _keyVersion.updateMapWithKeyValues(keyValues, jsonArrayKeyValues)
            }
            else
            {
                def value = String.valueOf(jsonElement)
                _keyVersion.updateMapWithKeyValue(keyValues, currentName, value)
            }
        }

        return keyValues
    }


    private def keyCount = new HashMap<String,Integer>()

    private def updateMapWithKeyValue(Map<String,String> originalMap, String key, String value) {

        if ( key == null || value == null )
        {
            return
        }

        if ( keyCount.containsKey(key) )
        {
            def indexedKey = buildIndexedKeyAndUpdateKeyCount(key)
            originalMap.put(indexedKey, value)
        }
        else
        {
            originalMap.put(key, value)
        }
    }

    private def String buildIndexedKeyAndUpdateKeyCount(String key) {

        def indexedKey = key

        if ( keyCount.containsKey(key) )
        {
            def keyIndex = keyCount.get(key) + 1
            indexedKey = key + "." + keyIndex
            keyCount.put(key, keyIndex)
        }
        else
        {
            indexedKey = key + "." + 1
            keyCount.put(key, 1)
        }

        return indexedKey
    }

    private def updateMapWithKeyValues(Map<String,String> originalMap, Map<String,String> additionalMap) {

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
}
