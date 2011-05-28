package net.config

import groovy.json.JsonSlurper

/**
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

    def Map<String,String> flattenJsonFile(String jsonFileName) {

        def jsSlurper = new JsonSlurper()
        def parsedFile = jsSlurper.parseText(jsonFileName.toURL().text)
        def flattenedKeyValues = flattenGroovyJsonObject(parsedFile)

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

    // todo: Refactor some of this into smaller methods
    def Map<String,String> transformGroovyJsonMap(Map jsonMap, String currentName) {

        if ( jsonMap == null || jsonMap.isEmpty() )
        {
            return new HashMap<String,String>()
        }

        def keyCount = 1
        def keyValues = new HashMap<String,String>()

        jsonMap.each { entry ->

            def key = entry.getKey()
            if ( currentName != null && !currentName.empty )
            {
                key = currentName + "." + key
            }

            if ( entry == null || entry.getValue() == null )
            {
                println("Null Entry Or Entry Value")
            }
            else if ( entry.getValue() instanceof List )
            {
                def jsonListKeyValues = transformJsonArray(entry.getValue(), key)
                keyValues.putAll(jsonListKeyValues)
            }
            else if ( entry.getValue() instanceof Map)
            {
                def jsonMapKeyValues = transformGroovyJsonMap(entry.getValue(), key)
                keyValues.putAll(jsonMapKeyValues)
            }
            else
            {
                def value = String.valueOf(entry.getValue())
                def keyVersion = new KeyVersion()
                keyValues.putAll(keyVersion.getKeyValues(keyCount, key, value, keyValues))
                keyCount = keyVersion._keyVersion
            }
        }

        return keyValues
    }

    // todo: refactor some of this into smaller methods
    def Map<String,String> transformJsonArray(List jsonArray, String currentName) {

        if ( jsonArray == null || jsonArray.isEmpty() )
        {
            return new HashMap<String, String>()
        }

        int keyCount = 1
        def keyValues = new HashMap<String,String>()

        jsonArray.each { jsonElement ->

            if ( jsonElement == null )
            {
                keyValues.put(currentName, null)
            }
            else if ( jsonElement instanceof Map)
            {
                def jsonMapKeyValues = transformGroovyJsonMap(jsonElement, currentName)
                def keyVersion = new KeyVersion()
                keyValues.putAll(keyVersion.getKeyValues(keyCount, keyValues, jsonMapKeyValues))
                keyCount = keyVersion._keyVersion
            }
            else if ( jsonElement instanceof List )
            {
                def jsonArrayKeyValues = transformJsonArray(jsonElement, currentName)
                def keyVersion = new KeyVersion()
                keyValues.putAll(keyVersion.getKeyValues(keyCount, keyValues, jsonArrayKeyValues))
                keyCount = keyVersion._keyVersion
            }
            else
            {
                def value = String.valueOf(jsonElement)
                def keyVersion = new KeyVersion()
                keyValues.putAll(keyVersion.getKeyValues(keyCount, currentName, value, keyValues))
                keyCount = keyVersion._keyVersion
            }
        }

        return keyValues
    }
}
