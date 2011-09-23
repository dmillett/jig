package net.util;

import java.util.regex.Pattern;

/**
 * Use to help create consistent patterns for configuration retrieval
 * from keyset values. This is limited to this library. It does
 * contain some underlying conventions to help improve config matching
 * and retrieval.
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
public class PatternHelper {

    /**
     * Creates a regex pattern with/without preceding ".*" for the first value
     * in 'text' and appends a ".*" between each value within 'text'.
     *
     * @param usePrefixWildcard Use a ".*" prefix for your regex --> forces use of match()
     * @param text An array of items to incorporate into the pattern
     * @return a regex pattern for the items in 'text' in order.
     */
    public static Pattern buildPattern(boolean usePrefixWildcard, String... text) {

        if ( text == null || text.length < 1 )
        {
            return Pattern.compile(".*");
        }

        StringBuilder sb;
        if ( usePrefixWildcard )
        {
            sb = new StringBuilder(".*");
        }
        else
        {
            sb = new StringBuilder();
        }

        for ( String s : text )
        {
            String corrected = s.replace(".", "\\.");
            sb.append(corrected).append(".*");
        }

        return Pattern.compile(sb.toString());
    }

    /**
     * Builds a regular expression by including, in order, every
     * value in 'text'. Each value in text will be separated by
     * a wildcard ".*" indicator in the pattern.
     *
     * Note that preceding wildcard characters will result in
     * using "match()" instead of "find()". This increases
     * retrieval latency. See 'useMatcherOverFind()'.
     *
     * @param text
     * @return
     */
    public static Pattern buildPattern(String... text) {
        return buildPattern(false, text);
    }

    /**
     * If there is a preceding wildcard '.*', then find()
     * performs poorly and matcher() should be used instead.
     * Otherwise, find() is quicker for retrieving matching keys.
     *
     * @param pattern A pattern to apply across a Map key set
     * @return true if wildcard ".*" prefixes the pattern, otherwise false.
     */
    public static boolean useMatcherOverFind(Pattern pattern) {

        if ( pattern == null || pattern.pattern() == null )
        {
            return false;
        }

        if ( pattern.pattern().startsWith(".*") )
        {
            return true;
        }

        return false;
    }
}
