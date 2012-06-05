package net.config.client

import java.util.regex.Pattern
import net.util.PatternHelper

/**
 * Groovy implementation of java PatternHelper methods. It is more succinct,
 * but I still use return types and parameter types to document the methods.
 *
 */
class ConfigLookup {

    def findByKeyPattern(configMap, pattern) {

        def configMatches = new HashMap<String, String>()
        def useMatcher = useMatch(pattern)

        configMap.each { entry ->

            String lowerCase = entry.key.toLowerCase()
            if ( !useMatcher && pattern.matcher(lowerCase).find() )
            {
                configMatches.put(entry.key, entry.value)
            }
            else if ( pattern.matcher(lowerCase).matches() )
            {
                configMatches.put(entry.key, entry.value)
            }
        }

        return configMatches
    }

    def boolean useMatch(pattern) {

        if ( pattern == null )
        {
            return false
        }

        if ( "${pattern}".startsWith(".*") )
        {
            return true
        }

        return false
    }

    def Pattern buildPattern(String text) {

        if ( text == null || text.isEmpty() )
        {
            return null;
        }

        String regex = text
        return ~/${regex}.*/
    }
}
