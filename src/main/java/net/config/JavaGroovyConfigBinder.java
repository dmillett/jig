package net.config;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Map;


/**
 *
 * @author dmillett
 */
public class JavaGroovyConfigBinder {

    private static final Logger LOG = Logger.getLogger(JavaGroovyConfigBinder.class);
    /** Curiously, Gradle will eat the errors during compilation if this path is wrong  */
    private static final String _groovyConfig = "src/main/groovy/net/config/ConfigController.groovy";

    private final GroovyObject _groovyObject;

    public JavaGroovyConfigBinder() {
        _groovyObject = instantiateGroovyObject(_groovyConfig);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getConfigMap() {

        if ( _groovyObject == null )
        {
            LOG.fatal("Cannot Retrieve Map From Null Groovy Object. Check Configuration Location And Groovy Files.");
            return null;
        }

        String methodName = "getConfig";
        Object[] args = {};
        Map<String, String> configKeyValues = (Map<String,String>)_groovyObject.invokeMethod(methodName, args);

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
