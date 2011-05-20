package net.config.example.two;

import junit.framework.TestCase;

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
public class PojoConfigExampleTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        System.getProperties().put("jConfigMap.location", "/home/dave/dev/jConfigMap/src/test/resources/config");
    }

    public void test__getAllStocks() {

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String, String> allStocks = pojo.findAllStocks();

        assertEquals(12, allStocks.size());
    }

    public void test__findStock() {

        PojoConfigExample pojo = new PojoConfigExample();

        // Use natural ordering
        String stock = pojo.findStock(null, "AMD");

        // The "sell-high" value should be first
        assertNotNull(stock);
        assertEquals("25.00", stock);
    }

    public void test__findChicagoBars() {

        PojoConfigExample pojo = new PojoConfigExample();
        Map<String,String> chicagoBars = pojo.findChicagoBars();

        assertEquals(4, chicagoBars.size());
    }
}
