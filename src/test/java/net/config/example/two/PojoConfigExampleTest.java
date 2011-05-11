package net.config.example.two;

import junit.framework.TestCase;

import java.util.Map;

/**
 * @author dmillett
 */
public class PojoConfigExampleTest
    extends TestCase {

    public void test__getAllStocks() {

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String, String> allStocks = pojo.findAllStocks();

        assertEquals(6, allStocks.size());
    }

    public void test__findStock() {

        PojoConfigExample pojo = new PojoConfigExample();

        // Use natural ordering
        String stock = pojo.findStock(null, "AMD");

        // The "sell-high" value should be first
        assertNotNull(stock);
        assertEquals("25.00", stock);
    }


}
