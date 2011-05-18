package net.config

import groovy.transform.Synchronized
import org.apache.log4j.Logger

/**
 * A simple cache map implementation for Groovy config lookups. Note that
 * it is only updated within synchronized blocks, everything thing else
 * is read-only for immutable values (String)
 * @author dmillett
 */
class ConfigController {

    private static final def Logger LOG = Logger.getLogger("ConfigController")
    private final static def CONFIG_CURRENT = new HashMap<String, String>();
    private final static def CONFIG_PREVIOUS = new HashMap<String, String>();

    def Object lock = new Object[0]
    def _configLoader = new ConfigLoader()


    // Correct -- this would trigger a load for every instantiation
    public ConfigController() {
        CONFIG_CURRENT.putAll(_configLoader.loadFromXmlFiles())
    }


    def Map<String, String> getConfig() {
        LOG.error("ConfigController.getConfig()")
        println "ConfigController.getConfig()"
        return CONFIG_CURRENT
    }

    @Synchronized('lock')
    def void reloadMap(Map newConfigValues) {

        if ( newConfigValues == null || newConfigValues.empty )
        {
            return
        }

        LOG.info("Loading New Configuration Values Into CURRENT_CONFIG")
        CONFIG_PREVIOUS.putAll(CONFIG_CURRENT)
        CONFIG_CURRENT.putAll(newConfigValues)
    }

    @Synchronized('lock')
    def void revertMap() {

        LOG.info("Reverting CONFIG_CURRENT To CONFIG_PREVIOUS")
        CONFIG_CURRENT.clear();
        CONFIG_CURRENT.putAll(CONFIG_PREVIOUS)
    }

    /**
     * Keys should not be changed, just their value. Otherwise there will probably
     * be code somewhere that shits the bed.
     *
     * @param newConfigValues
     * @return
     */
    def Map<String, String> compareNewAndExistingValues(Map newConfigValues) {

        if ( newConfigValues == null || newConfigValues.empty )
        {
            return
        }

        def currentKeys = CONFIG_CURRENT.keySet()
        def newKeys = newConfigValues.keySet();

        if ( !newKeys.containsAll(currentKeys) || newKeys.size() != currentKeys.size() )
        {
            // todo: list the missing keys
            return
        }

        // todo: find differences (if any)
    }

    def void dumpCurrentConfig() {

        def sb = new StringBuilder("Current Config:\n")
        CONFIG_CURRENT.entrySet().each { entry ->
            sb.append("$entry").append(",")
        }

        LOG.info(sb.toString())
    }
}
