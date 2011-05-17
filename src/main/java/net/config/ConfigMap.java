package net.config;

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

    private final Map<String, Map<String, String>> CURRENT_CONFIG = new HashMap<String, Map<String, String>>();
    private final Map<String, Map<String, String>> PREVIOUS_CONFIG = new HashMap<String, Map<String, String>>();
    private volatile boolean _emptyConfig = true;

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

    private synchronized void loadConfigMapFromFiles() {

        PREVIOUS_CONFIG.clear();
        PREVIOUS_CONFIG.putAll(CURRENT_CONFIG);

        JavaGroovyConfigBinder configBinder = new JavaGroovyConfigBinder();
        CURRENT_CONFIG.putAll(configBinder.getFileConfigMap());
    }
}
