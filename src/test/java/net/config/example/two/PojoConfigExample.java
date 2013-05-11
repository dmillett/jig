package net.config.example.two;

import net.client.ConfigLookup;
import net.util.GenericsHelper;
import org.apache.log4j.Logger;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
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
public class PojoConfigExample {

    private static final Logger LOG = Logger.getLogger(PojoConfigExample.class);
    private static final ConfigLookup _configLookup = new ConfigLookup();
    private static final Pattern STOCKS = Pattern.compile("stocks.stock.*");

    public ConfigLookup getConfigLookup() {
        return _configLookup;
    }

    public Map<String, String> findAllStocks() {
        return _configLookup.get(STOCKS);
    }

    // Be sure your params narrow it down to just one stock -- Perhaps Map could be sorted
    public String findStock(Comparator compare, String... stocks) {

        TreeMap<String,String> matches =
                (TreeMap<String,String>) _configLookup.getSortedResults(compare, STOCKS, stocks);

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


    private static final Pattern BARS = Pattern.compile(".*cities.*");

    public Map<String, String> findChicagoBars() {

        Map<String,String> chicagoBars = _configLookup.get(BARS, "Chicago", "Bars");
        return chicagoBars;
    }

    public Map<String,String> findAnnArborBars() {

        Map<String,String> annArborBors = _configLookup.get(BARS, "ann arbor", "bars");
        return annArborBors;
    }
}
