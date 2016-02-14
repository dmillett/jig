package net.config.example.one;

import net.client.ConfigLookup;
import net.util.ConfigStatistics;
import net.util.GenericsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * An enum style config handler.
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
public enum ConfigEnumExample {

    // key:value pairs
    ONE("key.one.string", String.class),
    TWO("key.two.int", Integer.class),
    THREE("key.three.double", Double.class),
    FOUR("key.four.boolean", Boolean.class),
    FIVE("key.five.list", List.class),

    // Structured Xml
    STOCKS("stocks.stock.*"),
    STOCK_HIGHS("stocks.*.sell-high"),
    STOCK_LOWS("stocks.*.sell-low"),
    STOCK_SHARES("stocks.*.shares"),
    STOCK_TWO("stocks.stock.*", "ExampleConfigTwo.xml"),

    COMMISSIONS("commissions.*"),
    COMMISSION_TYPES("commissions.commission.type.*"),
    COMMISSION_TICKERS("commissions.commission.type.*.ticker.*")

    ;

    private final Pattern _pattern;
    private final Class _clazz;
    private final String _configFileName;

    private final static GenericsHelper _helper = new GenericsHelper();
    private static final ConfigLookup _configLookup = new ConfigLookup();
    private static final Logger LOG = LoggerFactory.getLogger(ConfigEnumExample.class);

    private ConfigEnumExample(String regex, Class clazz, String configFileName) {
        _pattern = Pattern.compile(regex);
        _clazz = clazz;
        _configFileName = configFileName;
    }

    private ConfigEnumExample(String regex, Class clazz) {

        _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        _clazz = clazz;
        _configFileName = null;
    }

    private ConfigEnumExample(String regex, String configFileName) {

        _pattern = Pattern.compile(".*" + regex);
        _configFileName = configFileName;
        _clazz = null;
    }

    private ConfigEnumExample(String regex) {

        _pattern = Pattern.compile(".*" + regex);
        _clazz = null;
        _configFileName = null;
    }


    public Pattern getPattern() {
        return _pattern;
    }

    public Class getClazz() {
        return _clazz;
    }

    public String getConfigFileName() {
        return _configFileName;
    }

    public <T> T get(Class<T> clazz) {

        String value = _configLookup.getByKey(getPattern().pattern());
        if ( !clazz.equals(getClazz()) )
        {
            //LOG.warn("Class Type Mismatch For Key Lookup! Returning 'null'");
            return (T) value;
        }

        return _helper.get(value, clazz);
    }

    public <T> T getByFile(Class<T> clazz) {

        String value = _configLookup.getByKey(getConfigFileName(), getPattern().pattern());
        if ( !clazz.equals(getClazz()) )
        {
            LOG.warn("Class Type Mismatch For Key Lookup! Returning 'null'");
            return null;
        }

        return _helper.get(value, clazz);
    }

    public Map<String, String> get() {
        return _configLookup.get(getPattern());
    }

    public Map<String, String> get(String... params) {
        return _configLookup.get(getPattern(), params);
    }

    public Map<String, String> getByFile(String... params) {
        return  _configLookup.get(getConfigFileName(), getPattern(), params);
    }

    public static ConfigStatistics getConfigStatistics() {
        return _configLookup.getConfigStatistics();
    }
}
