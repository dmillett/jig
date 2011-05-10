package net.config;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 * @author dmillett
 */
public class ConfigLookup {

    /** Created just one time -- */
    private static JavaGroovyConfigBinder _groovyBinder = new JavaGroovyConfigBinder();

    public String get(String key) {
        return getConfigMapFromGroovy().get(key);
    }

    public Map<String,String> get(Pattern pattern) {

        Map<String,String> matches = new HashMap<String,String>();

        for ( Map.Entry<String, String> entry : getConfigMapFromGroovy().entrySet() )
        {
            if ( pattern.matcher(entry.getKey()).matches() )
            {
                matches.put(entry.getKey(), entry.getValue());
            }
        }

        return matches;
    }

    /**
     *
     * @param pattern
     * @param params
     * @return
     */
    public Map<String,String> get(Pattern pattern, String... params) {

        Map<String,String> matches = get(pattern);
        return reduce(matches, params);
    }

    /**
     *
     * @param originalMap
     * @param params
     * @return
     */
    public Map<String, String> reduce(Map<String, String> originalMap, String... params) {

        Map<String,String> reducedMap = new HashMap<String,String>(originalMap.size());

        for ( Map.Entry<String, String> entry : originalMap.entrySet() )
        {
            boolean match = true;
            for ( String param : params )
            {
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


    public Pattern buildPattern(String text) {

        if ( text == null || text.length() < 1 )
        {
            return Pattern.compile(".*");
        }

        return Pattern.compile(text);
    }

    /**  I don't like this setup -- need another way to specify file loads.  */
    private Map<String, String> getConfigMapFromGroovy() {
        return _groovyBinder.getConfigMap();
    }

    // todo
    public Pattern updatePattern(Pattern pattern, String ... params) {

        String originalPattern = pattern.pattern();
        String updatedPatternText = originalPattern;
        Pattern updatedPattern = Pattern.compile(updatedPatternText);

        return updatedPattern;
    }
}


