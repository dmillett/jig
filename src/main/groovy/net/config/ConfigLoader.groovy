package net.config

import org.apache.log4j.Logger

/**
 *
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
     * Load more than one file and store each config files map as a separate map
     *
     * @return
     */
    def Map<String, String> loadFromXmlFiles() {

        def keyValuesMap = new HashMap<String, String>()
        def files = loadConfigFiles()

        files.each { xmlFile ->
            keyValuesMap.putAll(loadFromXmlFile(xmlFile))
        }

        return keyValuesMap
    }

    /**
     * For example:
     * file1 -> Map<String,String> file1 config map
     * file2 -> Map<String,String> file2 config map
     * @return
     */
    def Map<String, Map<String, String>> loadMapsFromFiles() {

        def filesKeyValueMap = new HashMap<String, Map<String,String>>();
        def files = loadConfigFiles()

        files.each { xmlFile ->
            filesKeyValueMap.put(xmlFile, loadFromXmlFile(xmlFile))
        }

        return filesKeyValueMap
    }

    /**
     * Just loads XML files for now. Will adjust it to handle JSON files at some point.
     *
     * @return A list of xml file names for a specific directory (see CONFIG_LOCATION)
     */
    def List<String> loadConfigFiles() {

        def suffix = ~/.*\.xml/
        def location = System.getProperties().getProperty(CONFIG_LOCATION) + File.separator
        def configFiles = new ArrayList<String>()

        new File(location).eachFileMatch(suffix) { file ->
            def filePath = location + file.name
            configFiles.add(filePath)
        }

        return configFiles
    }
}
