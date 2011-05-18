package net.config.example.one;

import junit.framework.TestCase;
import net.config.example.JavaTestConfigHelper;
import net.config.example.one.ConfigEnumExample;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/8/11
 * Time: 3:13 PM
 * To change this template use File | Settings | File Templates.
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
        System.out.println(commissions);
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
