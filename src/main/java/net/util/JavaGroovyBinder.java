package net.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
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
 * limitations under the License.
 */
public class JavaGroovyBinder<T> {

    private static final Logger LOG = LoggerFactory.getLogger(JavaGroovyBinder.class);

    /**
     * A typical Java-Groovy binder to execute Groovy class files from within
     * Java code.
     *
     * @param groovyFileName The Groovy file with class to execute
     * @param methodName The Groovy class method to execute
     * @param args An paramters to call the Groovy method
     * @return Whatever the return type is
     * @deprecated Nice to have for information, but not used right now.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public T execute(String groovyFileName, String methodName, Object[] args) {

        try
        {
            ClassLoader parent = getClass().getClassLoader();
            GroovyClassLoader loader = new GroovyClassLoader(parent);
            Class groovyClass = loader.parseClass(new File(groovyFileName));
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            return (T)groovyObject.invokeMethod(methodName, args);
        }
        catch ( Exception e )
        {
            LOG.error("Problem Invoking Groovy Class:Method(" + groovyFileName + ":" + methodName + ")", e);
        }

        return null;
    }
}
