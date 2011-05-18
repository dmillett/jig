package net.config.example;

/**
 *
 * @author dmillett
 */
public class JavaTestConfigHelper {

    public static final String CONFIG_PROP = "jConfigMap.location";

    public static String updatePropertiesWithTestConfigPath() {

        if ( System.getProperty(CONFIG_PROP) == null )
        {
            String path = System.getProperty("user.dir") + "/src/test/resources/config/";
            System.setProperty(CONFIG_PROP, path);
        }

        return System.getProperty(CONFIG_PROP);
    }
}
