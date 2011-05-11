package net.config.example.two;

import net.config.ConfigLookup;
import net.util.GenericsHelper;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * @author dmillett
 */
public class PojoConfigExample {

    private static final Logger LOG = Logger.getLogger(PojoConfigExample.class);
    private static final ConfigLookup _configLookup = new ConfigLookup();


    private static final Pattern STOCKS = Pattern.compile(".*.stocks.stock.*");

    public Map<String, String> findAllStocks() {
        return _configLookup.get(STOCKS);
    }

    // Be sure your params narrow it down to just one stock -- Perhaps Map could be sorted
    public String findStock(Comparator compare, String... stocks) {

        TreeMap<String,String> matches = _configLookup.getSortedResults(compare, STOCKS, stocks);

        if ( matches.size() > 1 )
        {
            LOG.info("Returning First Iterator Value (ordered by comparator)");
        }

        return matches.firstEntry().getValue();
    }

    public Integer findStockShares(String... stockAndShares) {

        String stockShares = findStock(null, stockAndShares);
        GenericsHelper helper = new GenericsHelper();
        return helper.get(stockShares, Integer.class);
    }
}
