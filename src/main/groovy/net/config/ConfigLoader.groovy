package net.config

import org.apache.log4j.Logger

/**
 * @author dmillett
 */
class ConfigLoader {

    private static def LOG = Logger.getLogger(ConfigLoader.class)

    def Map<String, String> loadFromXmlFile(String file) {

        LOG.info("Loading Xml Config From File $file")
        println "println: ConfigLoader loading xml"

        if ( file != null && (new File(file)).exists() )
        {
            def xmlFlattener = new XmlFlattener();
            return xmlFlattener.flatten(file)
        }

        LOG.fatal("Could Not Load Configuration File $file. Returning Null")
        return null;
    }

    def Map<String, String> loadFromXmlFiles() {

        def tempFile = "/home/dave/dev/easy-config/src/test/resources/config/ExampleConfig.xml"
        return loadFromXmlFile(tempFile)
    }

    def Map<String, String> loadFromJsonFile(String file) {
        // todo:
        return new HashMap<String, String>(0)
    }

    def String findConfigLoadLocation() {


        return ""
    }

}
