package net.client;

import net.util.ConfigStatistics;
import net.util.GenericsHelper;
import net.util.PatternHelper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Use this to access the ConfigMap (cache) from Config POJOs or Enums.
 *
 * For property style key value pairs where there is a 1:1 match
 * use this for getting the exact config value.
 *
 * getByKey()
 *
 * When retrieving a group of config values pending config structure and
 * desired use:
 *
 * get()
 * getSortedResults()
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
public class ConfigLookup {

    private static final Logger LOG = Logger.getLogger(ConfigLookup.class);
    private static final ConfigMap CONFIG_MAP = new ConfigMap();

    /**
     * Build a pattern to apply across the keys in a Map. Matches
     * will be retrieved.
     *
     * @param text
     * @return
     * @deprecated See PatternHelper.buildPattern();
     */
    @Deprecated
    public Pattern buildPattern(String... text) {
        return PatternHelper.buildPattern(text);
    }

    /**
     * Use the filename as a namespace to expedite config single value lookup
     * instead of iterating across all files.
     *
     * @param fileName The config file that contains this single key-value property style entry
     * @param key There should be only one entry that matches this.
     * @return A String value for the single entry
     */
    public String getByKey(String fileName, String key) {

        if ( key == null )
        {
            return null;
        }

        if ( fileName == null )
        {
            LOG.info("Invalid File Name, Using Slower 'getByKey(key)'");
            return getByKey(key);
        }

        Map<String, String> configsForFile = CONFIG_MAP.getConfig().get(fileName);
        return configsForFile.get(key);
    }

    /**
     * If it is a simple key-value property style config, then pull it
     * from the Map and try to create its Primitive Object (or List).
     * See GenericsHelper for options.
     *
     * @param key An exact key name
     * @param clazz The class to return (Integer, Double, Long, Boolean, List)
     * @param <T> See 'clazz' for possible types
     * @return Any of the types listed above, or its original String value
     */
    public <T> T getByKey(String key, Class<T> clazz) {

        if ( key == null )
        {
            LOG.debug("Invalid Key, Returning 'null'");
            return null;
        }

        GenericsHelper helper = new GenericsHelper();
        String value = getByKey(key);

        return helper.get(value, clazz);
    }

    /**
     * Use the file name to narrow down the possible maps with the desired key.
     * @param fileName The file name with this specific config key
     * @param key The exact key name
     * @param clazz The desired type to cast to (primitives or List)
     * @param <T> See 'clazz' and GenericsHelper
     * @return A String or an object of type T
     */
    public <T> T getByKey(String fileName, String key, Class<T> clazz) {

        String result = getByKey(fileName, key);
        GenericsHelper helper = new GenericsHelper();

        return helper.get(result, clazz);
    }


    /**
     * Build a map of results based on a general or specific pattern applied to the
     * keys.
     *
     * @param pattern A pattern applied to each key
     * @return A hashmap for all key matches
     */
    public Map<String,String> get(Pattern pattern) {
        return getConfigMatches(CONFIG_MAP.getConfig(), pattern);
    }

    /**
     * Use the pattern to retrieve a Map of values and then reduce the
     * Map size using additional key names as a filter.
     *
     * @param pattern For Map key lookup
     * @param params Any additional portions of the key name to help reduce the Map
     * @return A map of key-value pairs according to 'pattern' and containing 'params'
     */
    public Map<String, String> get(Pattern pattern, String... params) {
        return getConfigMatches(CONFIG_MAP.getConfig(), pattern, params);
    }

    /**
     * Applies a comparator to the matched results via a TreeMap.
     *
     * @param comparator You call it (natural ordering if null)
     * @param pattern A pattern applied to the key set
     * @param params Any additionals keywords to reduce the keyset
     * @return A sorted map (TreeMap)
     */
    public Map<String, String> getSortedResults(Comparator<String> comparator, Pattern pattern, String... params) {

        Map<String,String> matches = getConfigMatches(CONFIG_MAP.getConfig(), pattern, params);
        TreeMap<String, String> treeMap = new TreeMap<String, String>(comparator);
        treeMap.putAll(matches);

        return treeMap;
    }

    /**
     * Use the Config file name to speed up retrieval for the desired pattern and params.
     *
     * @param fileName The filename that contains the text config (text, xml, json, etc)
     * @param pattern A pattern to apply to the keys
     * @param params Additional parameters that are part of the key names
     * @return A map of key-value pairs, for a given file, according to key pattern and containing params
     */
    public Map<String, String> get(String fileName, Pattern pattern, String... params) {

        if ( fileName == null || fileName.length() < 1 )
        {
            LOG.info("A Valid File Name Is Required To Lookup Config By File");
            return getConfigMatches(CONFIG_MAP.getConfig(), pattern, params);
        }

        Map<String, String> configsForFile = CONFIG_MAP.getConfig().get(fileName);
        return findMatches(configsForFile, pattern, params);
    }

    /**
     * Reduce the possible number of config matches with 'params' information
     * as it relates to the Config Map Key. For example, if the whole config
     * keys are:
     *
     * For pattern "foo.bar":
     *
     * "foo.bar.cheap.beer"
     * "foo.bar.expensive.beer"
     * "foo.bar.cheap.liquor"
     *
     * With params "cheap":
     *
     * "foo.bar.cheap.beer"
     * "foo.bar.cheap.liquor"
     *
     * With params "cheap, beer":
     *
     * "foo.bar.cheap.beer"
     *
     * @param originalMap A Map of matches for a given pattern
     * @param params Names that correspond to part of the map key (case insensitive)
     * @return A reduced set of matches for any of the given params
     */
    protected Map<String, String> reduce(Map<String, String> originalMap, String... params) {

        Map<String,String> reducedMap = new HashMap<String,String>(originalMap.size());
        List<String> lowerCaseParams = convertToLowerCase(params);

        for ( Map.Entry<String, String> entry : originalMap.entrySet() )
        {
            boolean match = true;
            for ( String param : lowerCaseParams )
            {
                if ( param == null ) { continue; }

                if ( !entry.getKey().contains(param) )
                {
                    match = false;
                    break;
                }
            }

            if ( match )
            {
                reducedMap.put(entry.getKey(), entry.getValue());
            }
        }

        return reducedMap;
    }

    /**
     * If it is a simple key-value property style config,then just pull it from
     * the map.
     *
     * @param key An exact key name
     * @return The stored value for 'key', otherwise null
     */
    public String getByKey(String key) {

        if ( key == null )
        {
            return null;
        }

        return getConfigValue(key);
    }

    /**
     * Loop through a Map of Maps to find the config. If you know the outer map
     * key, then it should be faster for larger config files. The outer map
     * key is the config file name.
     *
     * @param configMaps The file specific maps (file 1 --> map 1)
     * @param pattern A pattern to match against file map keysets
     * @param params A key must contain all of these params
     * @return A reduced key-value map
     */
    protected Map<String, String> getConfigMatches(Map<String, Map<String, String>> configMaps, Pattern pattern,
                                                   String... params) {

        Map<String, String> matches = new HashMap<String, String>();

        if ( configMaps == null || configMaps.isEmpty() )
        {
            return matches;
        }

        for ( Map<String, String> configMap : configMaps.values() )
        {
            matches.putAll(findMatches(configMap, pattern, params));
        }

        return matches;
    }

    /**
     * Find all Key matches, by pattern, for a given config map and then reduce by 'params'.
     *
     * @param configMap Any config map loaded from the config files
     * @param pattern To use against the Map keys
     * @param params Reduce by matching params in the Map key
     * @return A map of reduced results
     */
    protected Map<String, String> findMatches(Map<String, String> configMap, Pattern pattern, String... params) {

        if ( configMap == null || configMap.isEmpty() )
        {
            return new HashMap<String, String>();
        }

        return getConfigValues(configMap, pattern, params);
    }

    /** Experiment with find() vs matches() */
    private Map<String, String> getConfigValues(Map<String, String> configMap, Pattern pattern, String... params) {

        long start = 0;
        if ( ConfigStatistics.isEnabled() )
        {
            start = System.nanoTime();
        }

        boolean useMatcher = PatternHelper.useMatcherOverFind(pattern);
        Map<String, String> matches = new HashMap<String, String>();

        for ( Map.Entry<String, String> entry : configMap.entrySet() )
        {
            String lowerCaseKey = entry.getKey().toLowerCase();

            if ( !useMatcher && pattern.matcher(lowerCaseKey).find() )
            {
                matches.put(entry.getKey(), entry.getValue());
            }
            else if ( pattern.matcher(lowerCaseKey).matches() )
            {
                matches.put(entry.getKey(), entry.getValue());
            }
        }

        if ( !ConfigStatistics.isEnabled() )
        {
            return reduce(matches, params);
        }

        Map<String, String> reducedMap = reduce(matches, params);
        long lookupTime = System.nanoTime() - start;
        updateStats(reducedMap, pattern, lookupTime, params);

        return reducedMap;
    }

    /** Update the stats for each key match */
    private void updateStats(Map<String, String> reducedMap, Pattern pattern, long lookupTime, String... params) {

        String reducePattern = buildReducePatternRepresentation(pattern, params);

        for ( String key : reducedMap.keySet() )
        {
            ConfigStatistics.addKeyLookup(key, lookupTime, reducePattern);
        }
    }

    /** Generate a text representation of matching key items */
    private String buildReducePatternRepresentation(Pattern pattern, String... reducers) {

        if ( reducers == null )
        {
            return pattern.pattern();
        }

        StringBuilder sb = new StringBuilder(pattern.pattern());

        for ( String reducer : reducers )
        {
            sb.append(":").append(reducer);
        }

        return sb.toString();
    }

    /**
     * Retrieves a single value from the config map, if it exists. This is
     * used for property style config (1:1) lookups. If Statistics are
     * enabled (see ConfigStatistics), then it will add some latency, but
     * it will gather latency and count information for 'key'.
     *
     * @param key A property style key that should only be used for 1:1 mapping.
     * @return A single value or null if the key does not exist.
     */
    private String getConfigValue(String key) {

        long startTime = 0;

        if ( ConfigStatistics.isEnabled() )
        {
            startTime = System.nanoTime();
        }

        String result = null;
        for ( Map<String, String> configMap : CONFIG_MAP.getConfig().values() )
        {
            if ( configMap.containsKey(key) )
            {
                result = configMap.get(key);
                break;
            }
        }

        if ( !ConfigStatistics.isEnabled() )
        {
            return result;
        }

        // 1:1 lookup, so the key is the pattern
        long lookupTime = System.nanoTime() - startTime;
        ConfigStatistics.addKeyLookup(key, lookupTime, key);

        return result;
    }

    /**
     * Converts an array of string objects to lower case values.
     *
     * @param mixedCase An array of strings (mixed case or other)
     * @return A list of values where each 'mixedCase' has been downcased.
     */
    private List<String> convertToLowerCase(String[] mixedCase) {

        List<String> lowerCase = new ArrayList<String>();

        for (String s : mixedCase)
        {
            if ( s != null )
            {
                lowerCase.add(s.toLowerCase());
            }
        }

        return lowerCase;
    }
}
