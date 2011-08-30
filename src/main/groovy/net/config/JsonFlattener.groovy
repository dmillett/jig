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
            def urlFile = correctToValidUrlFile(jsonFileName)
            def parsedFile = jsSlurper.parseText(urlFile.text)

            if ( !validateIsValidConfig(parsedFile) )
            {
                LOG.info("Skipping Invalid JSON Config File $jsonFileName ($urlFile)")
                return new HashMap<String, String>(0);
            }

            flattenedKeyValues = flattenGroovyJsonObject(parsedFile)
        }
        catch ( Exception e )
        {
            LOG.error("Could Not Load JSON Configuration File", e)
            flattenedKeyValues = new HashMap<String,String>()
        }

        return flattenedKeyValues
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
     * Iterates through each Map entry and transforms any sub-maps or sub-arrays
     * therein. Otherwise, it is just a string "key" and "value".
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

    private def correctToValidUrlFile(urlFileName) {

        try
        {
            def urlFile = new URL(urlFileName)
            return urlFile
        }
        catch ( Exception e )
        {
            LOG.error("JSON Config Is Not A URL File", e)
        }

        return "file://${urlFileName}".toURL();
    }

    /**
     * The outer JSON value should be 'config' and it should be
     * the only key in the outer Map.
     *
     * @param groovyJsonMap
     * @return false if null or size != 1, otherwise true
     */
    private def validateIsValidConfig(groovyJsonMap) {

        if ( groovyJsonMap == null || groovyJsonMap.size() != 1 )
        {
            return false
        }

        groovyJsonMap.each { entry ->
            if ( !"config".equalsIgnoreCase(entry.key) )
            {
                return false
            }
        }

        return true
    }
}
