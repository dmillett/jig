package net.client;

import net.util.JavaGroovyConfigBinder;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This Config map loads the flattened config files via Groovy and is responsible
 * for providing them to the ConfigLookup utility class. The ConfigLookup can
 * then be used by any POJO or Enum config class.
 *
 * todo: expose some operations via JMX
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
public class ConfigMap {

    private static final Logger LOG = Logger.getLogger(ConfigMap.class);
    private static final Map<String, Map<String, String>> CURRENT_CONFIG = new HashMap<String, Map<String, String>>();
    private static final Map<String, Map<String, String>> PREVIOUS_CONFIG = new HashMap<String, Map<String, String>>();

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

    public synchronized void revertConfig() {

        CURRENT_CONFIG.clear();
        CURRENT_CONFIG.putAll(PREVIOUS_CONFIG);
    }

    public void compareAndLogDifferences() {

        compareMapFiles(CURRENT_CONFIG, PREVIOUS_CONFIG, "Previous Config Missing File");
        compareMapFiles(PREVIOUS_CONFIG, CURRENT_CONFIG, "Current Config Missing File");

        compareConfigMap(CURRENT_CONFIG, PREVIOUS_CONFIG, "Current Config", "Previous Config");
        compareConfigMap(PREVIOUS_CONFIG, CURRENT_CONFIG, "Previous Config", "Current Config");
    }

    public void dumpCurrentConfig() {
        dumpConfigMap(CURRENT_CONFIG, "Current Config");
    }

    public void dumpAllConfig() {
        dumpConfigMap(CURRENT_CONFIG, "Current Config");
        dumpConfigMap(PREVIOUS_CONFIG, "Previous Config");
    }

    private void dumpConfigMap(Map<String, Map<String,String>> map, String text) {

        LOG.info("Dumping Config For: " + text);
        for ( Map.Entry<String, Map<String, String>> fileEntry : map.entrySet() )
        {
            for ( Map.Entry<String, String> entry : map.get(fileEntry.getKey()).entrySet() )
            {
                LOG.info(entry.getKey() + ":" + entry.getValue());
            }
        }
    }

    /**
     * @return A copy of the Date that indicates the last config load occurrence.
     */
    public Date getLastUpdated() {
        return new Date(_lastUpdated.getTime());
    }

    /** Look at config files and their entries */
    private void compareConfigMap(Map<String, Map<String, String>> m1, Map<String, Map<String, String>> m2,
                                  String text1, String text2) {

        LOG.info("Items Missing/Changed From: " + text2 + " To " + text1);
        for ( String configFile : m1.keySet() )
        {
            Map<String,String> map2 = m2.get(configFile);
            compareEntries(m1, map2, configFile,text2);
        }
    }

    /** Look at config entries */
    private void compareEntries(Map<String, Map<String, String>> m1, Map<String, String> map2, String configFile,
                                String text) {

        for ( Map.Entry<String, String> entry : m1.get(configFile).entrySet() )
        {
            if ( map2 == null )
            {
                LOG.info(text + " Missing" + configFile + ":" + entry.getKey() + ", " + entry.getValue());
                continue;
            }

            String map2Value = map2.get(entry.getKey());

            if ( map2Value == null )
            {
                LOG.info(text + " Missing: '" + entry.getKey() + "'");
            }
            else if ( !entry.getValue().equals(map2Value) )
            {
                LOG.info("Key '" + entry.getKey() + "' Changed From: '" + map2Value + "' To:" + entry.getValue());
            }
        }
    }

    /** Look for missing config files for these two version */
    private void compareMapFiles(Map<String, Map<String,String>> m1, Map<String, Map<String,String>> m2, String msg) {

        for ( String keyOneFile : m1.keySet() )
        {
            if ( m1.containsKey(keyOneFile) && !m2.containsKey(keyOneFile) )
            {
                LOG.info(msg + ": " + keyOneFile);
            }
        }
    }

    /**
     * Locks the previous and current config maps for updates. It also
     * resets the '_lastUpdated' time stamp.
     *
     * This should be the only means to update the HashMap configs!
     */
    private synchronized void loadConfigMapFromFiles() {

        if ( !CURRENT_CONFIG.isEmpty() )
        {
            PREVIOUS_CONFIG.clear();
            PREVIOUS_CONFIG.putAll(CURRENT_CONFIG);
        }

        JavaGroovyConfigBinder configBinder = new JavaGroovyConfigBinder();
        CURRENT_CONFIG.putAll(configBinder.getFileConfigMap());

        if ( PREVIOUS_CONFIG.isEmpty() )
        {
            PREVIOUS_CONFIG.putAll(CURRENT_CONFIG);
        }

        _lastUpdated = new Date();
    }
}
