package net.config.example.one;

import junit.framework.TestCase;
import net.config.example.JavaTestConfigHelper;

import java.util.Map;

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
public class ConfigEnumStructuredXmlTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        JavaTestConfigHelper.updatePropertiesWithTestConfigPath();
    }

    public void test__STOCKS() {

        Map<String, String> allStocks = ConfigEnumExample.STOCKS.get();
        assertEquals(12, allStocks.size());
    }

    public void test__STOCKS_withArity() {

        Map<String, String> amdStocks = ConfigEnumExample.STOCKS.get("AMD");
        assertEquals(3, amdStocks.size());

        Map<String, String> amdShares = ConfigEnumExample.STOCKS.get("AMD", "shares");
        assertEquals(1, amdShares.size());

        String shareCount = amdShares.entrySet().iterator().next().getValue();
        assertEquals(200, Integer.parseInt(shareCount));
    }

    public void test__COMMISSIONS() {

        Map<String, String> commissions = ConfigEnumExample.COMMISSIONS.get();
        assertEquals(8, commissions.size());
    }

    public void test__COMMISSION_TYPES() {

        Map<String, String> types = ConfigEnumExample.COMMISSION_TYPES.get();
        assertEquals(8, types.size());

        Map<String, String> stockTypes = ConfigEnumExample.COMMISSION_TYPES.get("Stocks");
        assertEquals(6, stockTypes.size());
    }

    public void test__COMMISSION_TICKERS() {

        Map<String, String> tickers = ConfigEnumExample.COMMISSION_TICKERS.get();
        assertEquals(8, tickers.size());

        Map<String, String> amdTickers = ConfigEnumExample.COMMISSION_TICKERS.get("AMD");
        assertEquals(2, amdTickers.size());
    }

    public void test__STOCKS_TWO() {

        Map<String, String> stocksFileTwo = ConfigEnumExample.STOCK_TWO.get();
        assertEquals(12, stocksFileTwo.size());

    }
}
