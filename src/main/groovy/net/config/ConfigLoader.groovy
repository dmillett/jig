package net.config

import org.apache.log4j.Logger
import net.common.JConfigProperties
import java.util.regex.Pattern

/**
 * Load XML configuration files from "classpath/config" or "jConfigMap.location"
 * System property.  The configs will load in that order. Each config
 * file must have a valid form to get picked up.
 *
 * <pre>
 * <config>
 *   <!-- optional -->
 *   <keyValues>
 *   </keyValues>
 *
 *   <!-- or
 *       structured xml
 *   -->
 *
 *   <structures>
 *   </structures>
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

    private static def final LOG = Logger.getLogger(ConfigLoader.class)
    //private final def _supportedFiles = /.*\.xml/ //|json)/


    /**
     * Load a XML configuration file if that file exists and has the correct
     * nodes defined.
     *
     * @param file The XML file
     * @return
     */
    def Map<String, String> loadFromXmlFile(String file) {

        LOG.info("Loading XML Config From File ${file}")

        if ( file != null && (new File(file)).exists() )
        {
            def xmlFlattener = new XmlFlattener();
            return xmlFlattener.flatten(file)
        }

        LOG.fatal("Could Not Load XML Configuration File ${file}. Returning Null")
        return null;
    }

    /**
     * Load a JSON configuration file into a Map. First ensure the
     * file exists before loading it.
     *
     * @param jsonFileUrl Must be in URL format (file:// or http://)
     * @return A map of <string,string>. Null if no file, an empty map if there are problems with the file
     */
    def Map<String, String> loadFromJsonFile(String jsonFileUrl) {

        LOG.info("Loading JSON Config From File: ${jsonFileUrl}")

        if ( jsonFileUrl != null && (new File(jsonFileUrl.toString())).exists() )
        {
            def jsonFlattener = new JsonFlattener()
            return jsonFlattener.flatten(jsonFileUrl)
        }

        LOG.fatal("Could Not Load JSON Configuration File ${jsonFileUrl}. Returning Null")
        return null;
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
     * Load configs from XML or JSON
     * @param fileName A XML file name or url or a JSON file url
     * @return A map of key values regardless of config file type
     */
    def Map<String,String> loadKeyValuesFromFile(fileName) {

        if ( fileName == null )
        {
            return new HashMap<String,String>()
        }

        if ( fileName.endsWith("xml") )
        {
            return loadFromXmlFile(fileName)
        }
        else if ( fileName.endsWith("json") )
        {
            return loadFromJsonFile(fileName)
        }
        else if ( fileName.endsWith("class") || fileName.endsWith("jar") )
        {
            // todo

        }

        return new HashMap<String,String>()
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
     * todo: refactor this into smaller parts
     */
    def Map<String, String> loadFromFiles() {

        def keyValuesMap = new HashMap<String, String>()

        LOG.info("Loading Files From 'classpath/config' Location")
        updateWithClasspathConfigs(keyValuesMap)

        LOG.info("Loading Files From URL Location(s)")
        updateWithUrlConfigs(keyValuesMap)

        LOG.info("Loading Files From Override Location")
        updateWithOverrideConfigs(keyValuesMap)

        LOG.info("Loading Command Line Overrides")
        updateWithCommandLineOverrides(keyValuesMap)

        return keyValuesMap
    }


    private def updateWithCommandLineOverrides(Map<String, String> keyValuesMap) {

        def commandLineConfig = loadFromCommandLineSystemProperties()

        if ( commandLineConfig.isEmpty() )
        {
            return
        }

        LOG.info("Loaded ${commandLineConfig.size()} Config Command Line Overrides")
        updateWithOverrides(keyValuesMap, commandLineConfig)
    }


    private def updateWithOverrideConfigs(Map<String, String> keyValuesMap) {

        def overrideConfigs = loadConfigFilesFromOverride()

        if ( overrideConfigs.isEmpty())
        {
            return
        }

        def overrideKeyValues = new HashMap<String, String>()
        overrideConfigs.each { overrideFile ->
            overrideKeyValues.putAll(loadKeyValuesFromFile(overrideFile))
        }

        LOG.info("Loaded ${overrideConfigs.size()} Override Config Key-Values")
        updateWithOverrides(keyValuesMap, overrideKeyValues)
    }


    private def updateWithUrlConfigs(Map<String, String> keyValuesMap) {

        def urlConfigs = loadConfigsFromUrls()

        if ( urlConfigs.isEmpty() )
        {
            return
        }

        def urlConfigMap = new HashMap<String, String>()
        urlConfigs.each { urlFile ->
            urlConfigMap.putAll(loadKeyValuesFromFile(urlFile))
        }

        LOG.info("Loaded ${urlConfigMap.size()} From URL Location(s)")
        updateWithOverrides(keyValuesMap, urlConfigMap)
    }


    private def updateWithClasspathConfigs(Map<String,String>  keyValuesMap) {

        def classpathConfigs = loadConfigFilesFromClasspath()

        if ( classpathConfigs.isEmpty() )
        {
            return
        }

        classpathConfigs.each { classpathFile ->
            keyValuesMap.putAll(loadKeyValuesFromFile(classpathFile))
        }

        LOG.info("Loaded ${keyValuesMap.size()} Classpath Config Key-Values")
    }


    private def updateFilesMapConfigMapWithClasspathConfigs(Map<String, Map<String,String>> configMap) {

        def classpathFiles = loadConfigFilesFromClasspath()

        if ( classpathFiles.isEmpty() )
        {
            return
        }

        LOG.info("Loading Configs From Default Location: classpath/config")
        classpathFiles.each { classpathFile ->

            def classpathKeyValues = loadKeyValuesFromFile(classpathFile)
            if ( !classpathKeyValues.isEmpty() )
            {
                def shortName = shortenFileName(classpathFile)
                configMap.put(shortName, classpathKeyValues)
            }
        }
    }

    private def updateFilesMapConfigMapWithUrlConfigs(Map<String, Map<String,String>> configMap) {

        def urlFiles = loadConfigsFromUrls()

        if ( urlFiles.isEmpty() )
        {
            return;
        }

        LOG.info("Loading Configs From Remote URL(s)")
        urlFiles.each { urlFile ->
            def urlKeyValues = loadKeyValuesFromFile(urlFile)
            if ( !urlKeyValues.isEmpty() )
            {
                def shortName = shortenFileName(urlFile)
                configMap.put(urlFile, urlKeyValues)
            }
        }
    }

    /**
     * Include the file name, that has the config entries, as the outer map key.
     * For example:
     * file1 -> Map<String,String> file1 config map
     * file2 -> Map<String,String> file2 config map
     * @return A Map with shortened file name for the key and that files key-values
     * todo: refactor into smaller parts
     */
    def Map<String, Map<String, String>> loadMapsFromFiles() {

        def configMap = new HashMap<String, Map<String,String>>();

        updateFilesMapConfigMapWithClasspathConfigs(configMap)

        // todo
        def loadFromDatabase = null;

        updateFilesMapConfigMapWithUrlConfigs(configMap)

        updateFilesMapWithFileOverrides(configMap)

        updateFilesMapWithCommandLineOverrides(configMap)

        return configMap
    }

    private def updateFilesMapWithCommandLineOverrides(Map<String, Map<String, String>> configMap) {

        def commandLineOverrides = loadFromCommandLineSystemProperties()

        if (!commandLineOverrides.isEmpty())
        {
            LOG.info("Updating All File Maps From Command Line Overrides")
            updateAllMapsWithOverrides(configMap, commandLineOverrides)
        }
    }

    // Load from files in override location -- treat as override configs
    private def updateFilesMapWithFileOverrides(configMap) {

        def overrideFiles = loadConfigFilesFromOverride()

        if ( overrideFiles.isEmpty() )
        {
            return
        }

        LOG.info("Loading Configs From Override Location")
        overrideFiles.each { overrideFile ->

            def overrideLocationKeyValues = loadKeyValuesFromFile(overrideFile)
            if (!overrideLocationKeyValues.isEmpty())
            {
                def shortName = shortenFileName(overrideFile)
                configMap.put(shortName, overrideLocationKeyValues)

                LOG.info("Updating All File Maps From Override Config Location")
                updateAllMapsWithOverrides(configMap, overrideLocationKeyValues)
            }
        }
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
                    def overrideValue = overrideEntry.getValue()
                    def oldValue = fileMaps.get(fileKey).put(overrideKey, overrideValue)
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

            if ( !entry.getKey().startsWith(JConfigProperties.JCONFIG_COMMAND_LINE_PROP.getName()) )
            {
                return
            }

            def remove = JConfigProperties.JCONFIG_COMMAND_LINE_PROP.getName() + "."
            def overrideKey = "${entry.getKey()}".replace(remove, "")
            LOG.info("Overriding $overrideKey")
            commandLineOverrides.put(overrideKey, entry.getValue())
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
    def List<String> loadConfigsFromUrls() {

        def urlConfigs = new ArrayList<String>()
        System.getProperties().entrySet().each { entry ->

            if ( entry.getKey().startsWith(JConfigProperties.JCONFIG_URL_LOCATION.getName()) )
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

        def location = System.getProperty(JConfigProperties.JCONFIG_LOCATION.getName()) + File.separator
        def configFiles = new ArrayList<String>()
        def suffix = findFileNamePattern()

        new File(location).eachFileMatch(suffix) { file ->
            configFiles.add(file.toString())
        }

        return configFiles
    }

    /**
     * Supports the following file types:
     * XML
     * JSON
     *
     * todo: configure which file types get loaded
     * class
     * jar
     *
     * Supports case insensitive environment indicators. For example:
     * SomeConfig_dev.xml or SomeConfig_Dev.xml
     *
     * @return Only config files with that environment indicator are loaded OR all config files
     */
    def Pattern findFileNamePattern() {

        def fileSuffix = System.getProperty(JConfigProperties.JCONFIG_FILE_ENVIRONMENT.getName())
        if ( fileSuffix == null || fileSuffix.empty )
        {
            return ~/.*\.(xml|json)/
        }

        def pattern = ".*$fileSuffix\\.(xml|json)"
        return Pattern.compile(pattern, Pattern.CASE_INSENSITIVE)
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
        def codePath = codePathUrl.toString().substring(5)

        def filePattern = findFileNamePattern()
        def classpathConfigs = new ArrayList<String>()

        new File(codePath).eachDirRecurse { subDirectory ->

            def absPath = subDirectory.getAbsolutePath()
            if ( absPath.endsWith("config") && !absPath.contains("test") )
            {
                subDirectory.eachFileMatch(filePattern) { file ->
                    classpathConfigs.add(file.getCanonicalPath())
                }
            }
        }

        return classpathConfigs
    }

    def Map<String, Map<String, String>> loadFromDatabase(Map<String, Map<String, String>> loadedConfigs) {

    }
}
