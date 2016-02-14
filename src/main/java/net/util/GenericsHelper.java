package net.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * A useful tool (or not) to help with generic Objects and return types.
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
public class GenericsHelper {

    private static final Logger LOG = LoggerFactory.getLogger(GenericsHelper.class);

    /**
     * Useful for converting a "String" object into its primitive Java
     * wrapper object or Java List.
     *
     * @param value Any string value
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T get(String value, Class<T> clazz) {

        if ( clazz.equals(String.class) )
        {
            return (T)value;
        }

        try
        {
            if ( clazz.equals(Boolean.class) )
            {
                return (T) Boolean.valueOf(value);
            }
            else if ( clazz.equals(Integer.class) )
            {
                return (T) Integer.valueOf(value);
            }
            else if ( clazz.equals(Double.class) )
            {
                return (T) Double.valueOf(value);
            }
            else if ( clazz.equals(Long.class) )
            {
                return (T) Long.valueOf(value);
            }
            else if ( clazz.equals(List.class) )
            {
                List<String> results = Arrays.asList(value.split(","));
                return (T) results;
            }
        }
        catch ( Exception e )
        {
            LOG.error("Could Not Create List Or Primitive From: " + value + ", Returning: " + value, e);
        }

        return null;
    }

}
