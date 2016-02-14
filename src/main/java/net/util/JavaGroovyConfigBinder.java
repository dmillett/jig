package net.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOG = LoggerFactory.getLogger(JavaGroovyConfigBinder.class);

    private static final String _groovyConfigLoaderClass = "net.config.ConfigLoader";
    private final GroovyObject _groovyConfigLoader;

    public JavaGroovyConfigBinder() {
        _groovyConfigLoader = createGroovyObject(_groovyConfigLoaderClass);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Map<String,String>> getFileConfigMap() {

        if ( _groovyConfigLoader == null )
        {
            LOG.error("Cannot Retrieve Map From Null Groovy Object. Check Configuration Location And Groovy Files.");
            return null;
        }

        Map<String, Map<String,String>> configKeyValues = null;

        try
        {
            String methodName = "loadMapsFromFiles";
            Object[] args = {};
            configKeyValues = (Map<String, Map<String,String>>)_groovyConfigLoader.invokeMethod(methodName, args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return configKeyValues;
    }

    /**
     * Look this up from the Classloader (it better be in there).
     * @param className
     * @return
     */
    private GroovyObject createGroovyObject(String className) {

        try
        {
            Class groovyClass = Class.forName(className);
            return (GroovyObject) groovyClass.newInstance();
        }
        catch ( Exception e )
        {
            LOG.error("Cannot Create Instance Of Groovy Class: " + className);
            throw new RuntimeException(e);
        }
    }

    /**
     * Use for any Groovy object needed for execution within Java classes.
     *
     * @param groovySourceName
     * @return
     */
    private GroovyObject createGroovyObjectFromSource(String groovySourceName) {

        try
        {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class groovyClass = loader.parseClass(new File(groovySourceName));

            return (GroovyObject) groovyClass.newInstance();
        }
        catch ( Exception e )
        {
            LOG.error("Cannot Create Groovy Class From: " + groovySourceName, e);
            throw new RuntimeException(e);
        }
    }
}
