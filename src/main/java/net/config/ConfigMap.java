package net.config;

import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This Config map loads the flattened config files via Groovy and is responsible
 * for providing them to the ConfigLookup utility class. The ConfigLookup can
 * then be used by any POJO or Enum config class.
 *
 * @author dmillett
 */
public class ConfigMap {

    private static final Logger LOG = Logger.getLogger(ConfigMap.class);
    private final Map<String, Map<String, String>> CURRENT_CONFIG = new HashMap<String, Map<String, String>>();
    private final Map<String, Map<String, String>> PREVIOUS_CONFIG = new HashMap<String, Map<String, String>>();

    private volatile boolean _emptyConfig = true;
    private Date _lastUpdated;

    public Map<String, Map<String, String>> getConfig() {

        if ( _emptyConfig )
        {
            _emptyConfig = false;
            loadConfigMapFromFiles();
        }

        return CURRENT_CONFIG;
    }

    public void reloadConfigFiles() {
        loadConfigMapFromFiles();
    }

    public void revertConfig() {

        CURRENT_CONFIG.clear();
        CURRENT_CONFIG.putAll(PREVIOUS_CONFIG);
    }

    public void compareAndLogDifferences() {

        int currentSize = CURRENT_CONFIG.size();
        int previousSize = PREVIOUS_CONFIG.size();

        LOG.info("Current Config File Count: " + currentSize + ", Previous: "  + previousSize);

        compareLoadedConfigFileNames();


    }

    private void compareLoadedConfigFileNames() {

        for ( String fileName : CURRENT_CONFIG.keySet() )
        {
            if ( !PREVIOUS_CONFIG.containsKey(fileName) )
            {
                LOG.info("Current Config Removed From Previous: " + fileName);
            }
        }

        for ( String fileName : PREVIOUS_CONFIG.keySet() )
        {
            if ( !CURRENT_CONFIG.containsKey(fileName) )
            {
                LOG.info("Previous Config Removed From Current: " + fileName);
            }
        }
    }

    private synchronized void loadConfigMapFromFiles() {

        if ( !CURRENT_CONFIG.isEmpty() )
        {
            PREVIOUS_CONFIG.clear();
            PREVIOUS_CONFIG.putAll(CURRENT_CONFIG);
        }

        JavaGroovyConfigBinder configBinder = new JavaGroovyConfigBinder();
        CURRENT_CONFIG.putAll(configBinder.getFileConfigMap());
    }
}
