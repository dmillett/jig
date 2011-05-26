package net.config

import org.apache.log4j.Logger
import net.common.JConfigProperties

/**
 * Load XML configuration files from "classpath/config" or "jConfigMap.location"
 * System property.  The configs will load in that order. Each config
 * file must have a valid form to get picked up.
 *
 * <pre>
 * <config>
 *   <!-- optional -->
 *   <keyValueProperties>
 *   </keyValueProperties>
 *
 *   <!-- or
 *       structured xml
 *   -->
 *
 *   <xmlStructure>
 *   </xmlStructure>
 * </config>
 * </pre>
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
class ConfigLoader {

    private static def LOG = Logger.getLogger(ConfigLoader.class)
    //private static final def DEFAULT_CONFIG_LOCATION = ""

    /** Set/pull config files from this file location */
    //public static final def CONFIG_LOCATION = "jConfigMap.location"
    /** Set/pull from System properties with this prefix */
    //public static final def COMMAND_LINE_ARG = "jConfigMap.entry"
    /** Set/pull from System properties with this prefix */
    //public static final def CONFIG_URL = "jConfigMap.url"

    /**
     * Load a XML configuration file if that file exists and has the correct
     * nodes defined.
     *
     * @param file The XML file
     * @return
     */
    def Map<String, String> loadFromXmlFile(String file) {

        LOG.info("Loading Xml Config From File ${file}")

        if ( file != null && (new File(file)).exists() )
        {
            def xmlFlattener = new XmlFlattener();
            return xmlFlattener.flatten(file)
        }

        LOG.fatal("Could Not Load Configuration File ${file}. Returning Null")
        return null;
    }

    /**
     * Load more than one file and store in one large map.
     *
     * Load Order (reverse priority)
     * 1) classpath/config (local)
     * 2) config urls (remote)
     * 3) config file override location (local)
     * 4) command line entries (startup)
     *
     * @return
     */
    def Map<String, String> loadFromXmlFiles() {

        def keyValuesMap = new HashMap<String, String>()

        LOG.info("Loading Files From 'classpath/config' Location")
        def classpathConfigs = loadConfigFilesFromClasspath()
        if ( !classpathConfigs.isEmpty() )
        {
            classpathConfigs.each { xmlFile ->
                keyValuesMap.putAll(loadFromXmlFile(xmlFile))
            }
            LOG.info("Loaded ${keyValuesMap.size()} Classpath Config Key-Values")
        }

        LOG.info("Loading Files From URL Location(s)")
        def urlConfigs = loadXmlConfigsFromUrls()

        if ( !urlConfigs.isEmpty() )
        {
            def urlConfigMap = new HashMap<String,String>()
            urlConfigs.each { url ->
                urlConfigMap.putAll(loadFromXmlFile(url))
            }

            LOG.info("Loaded ${urlConfigMap.size()} From URL Location(s)")
            updateWithOverrides(keyValuesMap, urlConfigMap)
        }

        LOG.info("Loading Files From Override Location")
        def overrideConfigs = loadConfigFilesFromOverride()

        if ( !overrideConfigs.isEmpty() )
        {
            def overrideKeyValues = new HashMap<String, String>()
            overrideConfigs.each { xmlFile ->
                overrideKeyValues.putAll(loadFromXmlFile(xmlFile))
            }

            LOG.info("Loaded ${overrideConfigs.size()} Override Config Key-Values")
            updateWithOverrides(keyValuesMap, overrideKeyValues)
        }

        LOG.info("Loading Command Line Overrides")
        def commandLineConfig = loadFromCommandLineSystemProperties()

        if ( !commandLineConfig.isEmpty() )
        {
            LOG.info("Loaded ${commandLineConfig.size()} Config Command Line Overrides")
            updateWithOverrides(keyValuesMap, commandLineConfig)
        }

        return keyValuesMap
    }

    /**
     * Strip off the file path for shorter key.
     *
     * @param canonicalFilePath
     * @return
     */
    def String shortenFileName(String canonicalFilePath) {

        if ( canonicalFilePath == null || canonicalFilePath.isEmpty() )
        {
            return null
        }

        if ( canonicalFilePath.contains(File.separator) )
        {
            def shortened =  canonicalFilePath.substring(canonicalFilePath.lastIndexOf(File.separator) + 1)
            return shortened
        }

        return canonicalFilePath
    }

    /**
     * Include the file name, that has the config entries, as the outer map key.
     * For example:
     * file1 -> Map<String,String> file1 config map
     * file2 -> Map<String,String> file2 config map
     * @return
     */
    def Map<String, Map<String, String>> loadMapsFromFiles() {

        def filesKeyValueMap = new HashMap<String, Map<String,String>>();

        def classpathFiles = loadConfigFilesFromClasspath()
        if ( !classpathFiles.isEmpty() )
        {
            LOG.info("Loading Configs From Default Location: classpath/config")
            classpathFiles.each { classpathXmlFile ->

                def classpathKeyValues = loadFromXmlFile(classpathXmlFile)
                if ( !classpathKeyValues.isEmpty() )
                {
                    def shortName = shortenFileName(classpathXmlFile)
                    filesKeyValueMap.put(shortName, classpathKeyValues)
                }
            }
        }

        def urlFiles = loadXmlConfigsFromUrls()
        if ( !urlFiles.isEmpty() )
        {
            LOG.info("Loading Configs From Remote URL(s)")
            urlFiles.each { urlFile ->
                def urlKeyValues = loadFromXmlFile(urlFile)
                if ( !urlKeyValues.isEmpty() )
                {
                    def shortName = shortenFileName(urlFile)
                    filesKeyValueMap.put(urlFile, urlKeyValues)
                }
            }
        }

        def overrideFiles = loadConfigFilesFromOverride()
        if ( !overrideFiles.isEmpty() )
        {
            LOG.info("Loading Configs From Override Location")
            overrideFiles.each { xmlFile ->
                def overrideLocationKeyValues = loadFromXmlFile(xmlFile)
                if ( !overrideLocationKeyValues.isEmpty() )
                {
                    def shortName = shortenFileName(xmlFile)
                    filesKeyValueMap.put(shortName, overrideLocationKeyValues)

                    LOG.info("Updating All File Maps From Override Config Location")
                    updateAllMapsWithOverrides(filesKeyValueMap, overrideLocationKeyValues)
                }
            }
        }

        def commandLineOverrides = loadFromCommandLineSystemProperties()
        if ( !commandLineOverrides.isEmpty() )
        {
            LOG.info("Updating All File Maps From Command Line Overrides")
            updateAllMapsWithOverrides(filesKeyValueMap, commandLineOverrides)
        }

        return filesKeyValueMap
    }

    /**
     * Updates the original "classpath" map with override map values. It will dump
     * any differences/replacements to the Log file.
     *
     * @param original The config map from classpath/config location
     * @param overrides The config map from overrides location
     */
    private def updateWithOverrides(Map<String,String> original, Map<String,String> overrides) {

        overrides.entrySet().each { entry ->

            if ( original.containsKey(entry.getKey()) )
            {
                def key = entry.getKey()
                LOG.info("Overriding ${key}: '${original.get(key)}' With '${entry.getValue()}'")
            }

            original.put(entry.getKey(), entry.getValue())
        }
    }

    /**
     * Iterates through each file:config-key entry and replaces all key-values that with the
     * override value. Run this after the override map is finalized.
     *
     * @param fileMaps Each config map per config file (uses filename as a namespace)
     * @param overrides Overrides from the override file location, url, or command line property.
     *
     * @return The original filename:key-value with updates (note: side effects)
     */
    private def updateAllMapsWithOverrides(Map<String, Map<String,String>> fileMaps, Map<String,String> overrides) {

        fileMaps.keySet().each { fileKey ->

            overrides.entrySet().each { overrideEntry ->

                String overrideKey = overrideEntry.getKey()
                if ( fileMaps.get(fileKey).containsKey(overrideKey) )
                {
                    String overrideValue = overrideEntry.getValue()
                    String oldValue = fileMaps.get(fileKey).put(overrideKey, overrideValue)
                    LOG.info("Replaced ${fileKey}:${overrideKey} '${oldValue}' With '${overrideValue}'")
                }
            }
        }
    }

    /**
     * Load specific key-values from command line arguments that are part
     * of System.properties.
     *
     * See "COMMAND_LINE_ARG" for the proper prefix
     *
     * @return A Map of all the command line config properties (empty map if none specified)
     */
    def Map<String,String> loadFromCommandLineSystemProperties() {

        def commandLineOverrides = new HashMap<String,String>()
        System.getProperties().entrySet().each { entry ->

            if ( entry.getKey().startsWith(JConfigProperties.jCONFIG_COMMAND_LINE_PROP.getName()) ) //COMMAND_LINE_ARG) )
            {
                commandLineOverrides.put(entry.getKey(), entry.getValue())
            }
        }

        return commandLineOverrides
    }

    /**
     * Groovy can parse XMLs from a specific URL, this method helps find
     * those URLs from command line System properties.
     *
     * See "CONFIG_URL" for the proper prefix
     *
     * @return A list of xml configs defined in System properties.
     */
    def List<String> loadXmlConfigsFromUrls() {

        def urlConfigs = new ArrayList<String>()
        System.getProperties().entrySet().each { entry ->

            if ( entry.getKey().startsWith(JConfigProperties.jCONFIG_URL_LOCATION.getName()) ) //CONFIG_URL) )
            {
                urlConfigs.add(entry.getValue())
            }
        }

        return urlConfigs
    }

    /**
     * Just loads XML files for now. Will adjust it to handle JSON files at some point.
     * It relies on CONFIG_LOCATION to lookup the config files.
     *
     * @return A list of xml file names for a specific directory (see CONFIG_LOCATION)
     */
    def List<String> loadConfigFilesFromOverride() {

        def location = System.getProperty(JConfigProperties.jCONFIG_LOCATION.getName()) + File.separator
        def configFiles = new ArrayList<String>()
        def suffix = ~/.*\.xml/

        new File(location).eachFileMatch(suffix) { file ->
            configFiles.add(file.toString())
        }

        return configFiles
    }

    /**
     * Load these configuration files from the "'classpath'/config" directory.
     * It only loads "xml" files for now.
     *
     * @return A list of xml filenames (and paths)
     */
    def List<String> loadConfigFilesFromClasspath() {

        // Trim of the "file:" prefix from the classpath
        def codePathUrl = getClass().getProtectionDomain().codeSource.location
        def codePath = codePathUrl.toString().substring(5) //+ "config"

        def xmlFilePattern = ~/.*\.xml/
        def classpathConfigs = new ArrayList<String>()

        new File(codePath).eachDirRecurse {subDirectory ->

            def absPath = subDirectory.getAbsolutePath()
            if ( absPath.endsWith("config") && !absPath.contains("test") )
            {
                subDirectory.eachFileMatch(xmlFilePattern) { file ->
                    classpathConfigs.add(file.getCanonicalPath())
                }
            }
        }

        return classpathConfigs
    }
}
