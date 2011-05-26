package net.config

import groovy.json.JsonSlurper

/**
 *
 *
 */
class JsonFlattener {

    def Map<String,String> flattenJsonFile(String jsonFileName) {

        def jsSlurper = new JsonSlurper()
        def parsedFile = jsSlurper.parseText(jsonFileName.toURL().text)

        def flattenedKeyValues = transformGroovyJsonMap(parsedFile, "")
        println("")
        println("flattenedKeyValues: ${flattenedKeyValues}")

        return flattenedKeyValues
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
                key = currentName + "." + entry.getKey()
            }

            // Either a Map, Array, or String here
            if ( entry == null || entry.getValue() == null )
            {
                println("Null Entry Or Entry Value")
            }
            else if ( entry.getValue() instanceof List )
            {
                def jsonListKeyValues = transformJsonArray(entry.getValue(), key)
                if ( !jsonListKeyValues.isEmpty() )
                {
                    keyValues.putAll(jsonListKeyValues)
                }
            }
            else if ( entry.getValue() instanceof Map)
            {
                //println "JsonMap Map entry value dump: ${entry.getValue().dump()}"
                def jsonMapKeyValues = transformGroovyJsonMap(entry.getValue(), key)
                if ( !jsonMapKeyValues.isEmpty() )
                {
                    keyValues.putAll(jsonMapKeyValues)
                }
            }
            else if ( entry.getValue() instanceof String )
            {
                //println "JsonMap 'else' entry value dump: ${entry.getValue().dump()}"
                if ( keyValues.containsKey(key) )
                {
                    def indexedKey = key + "." + keyCount
                    keyCount++
                    keyValues.put(indexedKey, entry.getValue())
                    println("indexedKey: ${indexedKey}")
                }
                else
                {
                    keyValues.put(key, entry.getValue())
                }

                //println("key: ${key}, keyValues: ${keyValues}")
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
            if ( jsonElement != null && jsonElement instanceof Map)
            {
                def jsonMapKeyValues = transformGroovyJsonMap(jsonElement, currentName)
                jsonMapKeyValues.entrySet().each { entry ->
                    if ( keyValues.containsKey(entry.getKey()) )
                    {
                        def indexedKey = entry.getKey() + "." + keyCount
                        keyCount++
                        keyValues.put(indexedKey, entry.getValue())
                    }
                    else
                    {
                        keyValues.put(entry.getKey(), entry.getValue())
                    }
                }
            }
            else if ( jsonElement != null && jsonElement instanceof List )
            {
                println("non map value: ${jsonElement.dump()}")
            }
            else if ( jsonElement != null && jsonElement instanceof String )
            {
                if ( keyValues.containsKey(currentName) )
                {
                    def indexedKey = currentName + "." + keyCount
                    keyCount++
                    keyValues.put(indexedKey, jsonElement)
                }
                else
                {
                    keyValues.put(currentName, jsonElement)
                }
            }
            else
            {
                println "dammit null"
            }
        }

        return keyValues
    }

}
