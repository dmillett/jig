package net.config

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/18/11
 * Time: 4:43 PM
 * To change this template use File | Settings | File Templates.
 */
class GroovyTestConfigHelper {

    def static String updateSystemPropertyConfigLocation() {

        if ( System.getProperty(ConfigLoader.CONFIG_LOCATION) == null )
        {
            def testConfigPath = new File('').absolutePath + "/src/test/resources/config/"
            System.setProperty(ConfigLoader.CONFIG_LOCATION, testConfigPath)
        }

        return System.getProperty(ConfigLoader.CONFIG_LOCATION)
    }
}
