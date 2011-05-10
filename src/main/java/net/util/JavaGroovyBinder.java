package net.util;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/7/11
 * Time: 10:27 AM
 * To change this template use File | Settings | File Templates.
 */
public class JavaGroovyBinder<T> {

    private static final Logger LOG = Logger.getLogger(JavaGroovyBinder.class);

    /**
     * A typical Java-Groovy binder to execute Groovy class files from within
     * Java code.
     *
     * @param groovyFileName
     * @param methodName
     * @param args
     * @return
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
