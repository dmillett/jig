package net.util;

import junit.framework.TestCase;
import net.config.example.JavaTestConfigHelper;
import net.config.example.one.ConfigEnumExample;
import net.config.example.two.PojoConfigExample;

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
 * limitations under the License.
 */
public class ConfigStatisticsTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        JavaTestConfigHelper.updatePropertiesWithTestConfigPath();;
    }

    public void test__enable_disable_statistics() {

        assertFalse(ConfigStatistics.isEnabled());

        ConfigStatistics.enableStatsCollection();
        assertTrue(ConfigStatistics.isEnabled());

        ConfigStatistics.disableStatsCollection();
        assertFalse(ConfigStatistics.isEnabled());
    }

    public void test__getAllStocks_without_statistics() {

        if ( ConfigStatistics.isEnabled() )
        {
            ConfigStatistics.disableStatsCollection();
        }

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String, String> allStocks = pojo.findAllStocks();

        assertEquals(12, allStocks.size());
        assertEquals(0, ConfigStatistics.getStats().size());
    }

    public void test__getAllStocks_with_statistics_single_pass() {

        ConfigStatistics.enableStatsCollection();

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String, String> allStocks = pojo.findAllStocks();
        // Sometimes a multi-threaded test seems to throw this count off (bad gradle)
        assertEquals(12, allStocks.size());

        ConfigStatistics.disableStatsCollection();

        Map<String, StatsValue> stats = ConfigStatistics.getStats();
        assertEquals(12, stats.size());

        for ( Map.Entry<String, StatsValue> entry : stats.entrySet() )
        {
            StatsValue value = entry.getValue();
            assertTrue(value.getAverageLatency() > 0.0);
            assertTrue(value.getLastAccessed() > 0);
            assertEquals(1, value.getCount());
            assertEquals(1, value.getAssociatedPatterns().size());
        }
    }

    public void test_getAllStocks_with_statistics_multiple_passes() {

        ConfigStatistics.enableStatsCollection();

        Map<String, String> amdTickers = ConfigEnumExample.COMMISSION_TICKERS.get("AMD");
        assertEquals(2, amdTickers.size());

        Map<String, String> intcTickers = ConfigEnumExample.COMMISSION_TICKERS.get("INTC");
        assertEquals(1, intcTickers.size());

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String, String> allStocks = pojo.findAllStocks();
        assertEquals(12, allStocks.size());

        ConfigStatistics.disableStatsCollection();

        Map<String, StatsValue> stats = ConfigStatistics.getStats();
        assertEquals(15, stats.size());

        boolean countGreaterThanOne = false;
        for ( Map.Entry<String, StatsValue> entry : stats.entrySet() )
        {
            if ( entry.getValue().getCount() > 1 )
            {
                countGreaterThanOne = true;
                break;
            }
        }

        assertTrue(countGreaterThanOne);
    }
}
