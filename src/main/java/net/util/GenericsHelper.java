package net.util;

import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/9/11
 * Time: 8:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class GenericsHelper {

    private static final Logger LOG = Logger.getLogger(GenericsHelper.class);

    public <T> T get(String value, Class<T> clazz) {

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

        return (T)value;
    }

}
