package net.client;

import junit.framework.TestCase;
import net.config.example.JavaTestConfigHelper;
import net.util.ConfigStatistics;
import net.util.PatternHelper;

import java.util.Map;
import java.util.regex.Pattern;

/**
 *
 *
 * Look at:
 * 1) functionality
 * 2) performance
 * 3) pattern performance
 * 4) loop reduce vs pattern reduce
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
 * limitations under the License.
 */
public class ConfigLookupTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        JavaTestConfigHelper.updatePropertiesWithTestConfigPath();
    }

    public void test__get_with_pattern() {

        Pattern wildcard = PatternHelper.buildPattern("stock.amd");
        ConfigLookup cfgHelper = new ConfigLookup();
        Map<String, String> patternMatches = cfgHelper.get(wildcard);

        assertEquals(6, patternMatches.size());
    }

    public void test__get_with_pattern2() {

        Pattern wildcard = PatternHelper.buildPattern("stock.amd");
        ConfigLookup cfgHelper = new ConfigLookup();
        Map<String, String> patternMatches = cfgHelper.get(wildcard);

        assertEquals(6, patternMatches.size());
    }

    public void test_get_with_pattern_performance() {

        Pattern pattern = PatternHelper.buildPattern("amd");
        int iterations = 100;
        long startTime = System.nanoTime();

        retrievalPerformanceLoop(iterations, pattern);

        long executionTime = System.nanoTime() - startTime;
        double milis = executionTime * Math.pow(10.0, -6.0);
        String output = String.format("Peformance For: %1d Iterations Is: %2f (ms)", iterations, milis);
        //System.out.println(output);
    }

    // Single threaded performance -- should be on ~par.
    public void test__config_stats_performance() {

        Pattern pattern = PatternHelper.buildPattern("amd");

        int iterations = 100;
        long startTime = System.nanoTime();

        retrievalPerformanceLoop(iterations, pattern);

        long executionTime = System.nanoTime() - startTime;
        double milis = executionTime * Math.pow(10.0, -6.0);
        String output = String.format("Peformance For: %1d Iterations Is: %2f (ms)", iterations, milis);
        //System.out.println(output);

        ConfigStatistics.enableStatsCollection();

        startTime = System.nanoTime();
        retrievalPerformanceLoop(iterations, pattern);
        executionTime = System.nanoTime() - startTime;

        ConfigStatistics.disableStatsCollection();

        milis = executionTime * Math.pow(10.0, -6.0);
        output = String.format("Peformance With Stats For: %1d Iterations Is: %2f (ms)", iterations, milis);

        ConfigStatistics.clearStatistics();
        //System.out.println(output);
    }

    public void test__getByKey() {

        ConfigLookup cfg = new ConfigLookup();

        String key = "key.one.string";
        String fileName = "ConfigOne.xml";

        String value = cfg.getByKey(fileName, key, String.class);
        assertEquals("first value", value);
    }

    public void test__getByKey_with_pattern() {

        ConfigLookup cfg = new ConfigLookup();
        Pattern p1 = PatternHelper.buildPattern("amd");
        String fileName = "ConfigOne.xml";

        Map<String, String> amdConfigs = cfg.get(fileName, p1);
        assertEquals(5, amdConfigs.size());
    }


    private void retrievalPerformanceLoop(int iterations, Pattern pattern) {

        ConfigLookup lookup = new ConfigLookup();
        int count = 0;

        while ( count < iterations )
        {
            lookup.get(pattern);
            count++;
        }
    }
}
