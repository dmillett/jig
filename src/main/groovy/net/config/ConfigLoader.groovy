package net.config

import org.apache.log4j.Logger

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
 */
class ConfigLoader {

    private static def LOG = Logger.getLogger(ConfigLoader.class)
    private static final def DEFAULT_CONFIG_LOCATION = ""
    public static final def CONFIG_LOCATION = "jConfigMap.location"

    def Map<String, String> loadFromXmlFile(String file) {

        LOG.info("Loading Xml Config From File $file")

        if ( file != null && (new File(file)).exists() )
        {
            def xmlFlattener = new XmlFlattener();
            return xmlFlattener.flatten(file)
        }

        LOG.fatal("Could Not Load Configuration File $file. Returning Null")
        return null;
    }

    /**
     * Load more than one file and store each config files map as a separate map.
     * It will first load from the "classpath/config" directory, then
     * it will load from the override directory if defined in the
     * System.properties (see CONFIG_LOCATION)
     *
     * @return
     */
    def Map<String, String> loadFromXmlFiles() {

        def keyValuesMap = new HashMap<String, String>()

        LOG.info("Loading Files From 'classpath/config' Location")
        def classpathConfigs = loadConfigFilesFromClasspath()
        classpathConfigs.each { xmlFile ->
            keyValuesMap.putAll(loadFromXmlFile(xmlFile))
        }
        LOG.info("Loaded ${keyValuesMap.size()} Classpath Config Key-Values")

        LOG.info("Loading Files From Override Location")
        def overrideKeyValues = new HashMap<String, String>()
        def overrideConfigs = loadConfigFilesFromOverride()

        overrideConfigs.each { xmlFile ->
            overrideKeyValues.putAll(loadFromXmlFile(xmlFile))
        }
        LOG.info("Loaded ${overrideConfigs.size()} Override Config Key-Values")

        updateWithOverrides(keyValuesMap, overrideKeyValues)

        return keyValuesMap
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

        println original
    }

    /**
     * For example:
     * file1 -> Map<String,String> file1 config map
     * file2 -> Map<String,String> file2 config map
     * @return
     */
    def Map<String, Map<String, String>> loadMapsFromFiles() {

        def filesKeyValueMap = new HashMap<String, Map<String,String>>();
        def files = loadConfigFilesFromOverride()

        files.each { xmlFile ->
            def keyValues = loadFromXmlFile(xmlFile)
            if ( !keyValues.isEmpty() )
            {
                filesKeyValueMap.put(xmlFile, loadFromXmlFile(xmlFile))
            }
        }

        return filesKeyValueMap
    }

    /**
     * Just loads XML files for now. Will adjust it to handle JSON files at some point.
     * It relies on CONFIG_LOCATION to lookup the config files.
     *
     * @return A list of xml file names for a specific directory (see CONFIG_LOCATION)
     */
    def List<String> loadConfigFilesFromOverride() {

        def location = System.getProperty(CONFIG_LOCATION) + File.separator
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
        def codePath = codePathUrl.toString().substring(5) + "config"

        def suffix = ~/.*\.xml/
        def configFiles = new ArrayList<String>()

        new File(codePath).eachFileMatch(suffix) { configFile ->
            configFiles.add(configFile.toString())
        }

        return configFiles
    }
}
