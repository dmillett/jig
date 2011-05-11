package net.config.example.one;

import net.config.ConfigLookup;
import net.util.GenericsHelper;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;


/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/5/11
 * Time: 10:18 PM
 * To change this template use File | Settings | File Templates.
 */
public enum ConfigEnumExample {

    // key:value pairs
    ONE("key.one.string", String.class),
    TWO("key.two.int", Integer.class),
    THREE("key.three.double", Double.class),
    FOUR("key.four.boolean", Boolean.class),
    FIVE("key.five.list", List.class),

    // Structured Xml
    STOCKS("stocks.stock.*"),
    STOCK_HIGHS("stocks.*.sell-high"),
    STOCK_LOWS("stocks.*.sell-low"),
    STOCK_SHARES("stocks.*.shares"),

    COMMISSIONS("commissions.*"),
    COMMISSION_TYPES("commissions.commission.type.*"),
    COMMISSION_TICKERS("commissions.commission.type.*.ticker.*")

    ;

    private final Pattern _pattern;
    private final Class _clazz;

    private final static GenericsHelper _helper = new GenericsHelper();
    private static final ConfigLookup _configLookup = new ConfigLookup();

    ConfigEnumExample(String regex, Class clazz) {

        _pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        _clazz = clazz;
    }

    ConfigEnumExample(String regex) {

        _pattern = Pattern.compile(".*" + regex);
        _clazz = null;
    }

    public Pattern getPattern() {
        return _pattern;
    }

    public Class getClazz() {
        return _clazz;
    }

    public <T> T get(Class<T> clazz) {

        String value = _configLookup.getByKey(getPattern().pattern());
        if ( !clazz.equals(getClazz()) )
        {
            return (T) value;
        }

        return _helper.get(value, clazz);
    }

    public Map<String, String> get() {
        return _configLookup.get(getPattern());
    }

    public Map<String, String> get(String... params) {
        return _configLookup.get(getPattern(), params);
    }
}
