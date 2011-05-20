package net.config;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;


/**
 * Bind a Groovy object to load configuration files. See ConfigLoader.groovy.
 * That provides a flattened map where the corresponding filename prefixes
 * each config key entry.
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
public class JavaGroovyConfigBinder {

    private static final Logger LOG = Logger.getLogger(JavaGroovyConfigBinder.class);
    /** Curiously, Gradle will eat the errors during compilation if this path is wrong  */
    @Deprecated
    private static final String _groovyConfigMapClass = "src/main/groovy/net/config/ConfigController.groovy";
    private static final String _groovyConfigLoaderClass = "src/main/groovy/net/config/ConfigLoader.groovy";

    @Deprecated
    private final GroovyObject _groovyConfigMap;
    private final GroovyObject _groovyConfigLoader;

    public JavaGroovyConfigBinder() {
        _groovyConfigMap = instantiateGroovyObject(_groovyConfigMapClass);
        _groovyConfigLoader = instantiateGroovyObject(_groovyConfigLoaderClass);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String,String>> getFileConfigMap() {

        if ( _groovyConfigLoader == null )
        {
            LOG.fatal("Cannot Retrieve Map From Null Groovy Object. Check Configuration Location And Groovy Files.");
            return null;
        }

        String methodName = "loadMapsFromFiles";
        Object[] args = {};
        Map<String, Map<String,String>> configKeyValues =
                (Map<String, Map<String,String>>)_groovyConfigLoader.invokeMethod(methodName, args);

        return configKeyValues;
    }

    @Deprecated
    @SuppressWarnings("unchecked")
    public Map<String, String> getConfigMap() {

        if ( _groovyConfigMap == null )
        {
            LOG.fatal("Cannot Retrieve Map From Null Groovy Object. Check Configuration Location And Groovy Files.");
            return null;
        }

        String methodName = "getConfig";
        Object[] args = {};
        Map<String, String> configKeyValues = (Map<String,String>) _groovyConfigMap.invokeMethod(methodName, args);

        return configKeyValues;
    }

    /**
     * Use for any Groovy object needed for execution within Java classes.
     *
     * @param groovyFileName
     * @return
     */
    public GroovyObject instantiateGroovyObject(String groovyFileName) {

        try
        {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class groovyClass = loader.parseClass(new File(groovyFileName));

            return (GroovyObject) groovyClass.newInstance();
        }
        catch ( Exception e )
        {
            LOG.fatal("Cannot Create Groovy Class From: " + groovyFileName, e);
            throw new RuntimeException(e);
        }
    }
}
