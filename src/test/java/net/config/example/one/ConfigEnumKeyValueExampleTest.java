package net.config.example.one;

import junit.framework.TestCase;
import net.config.example.one.ConfigEnumExample;

import java.util.List;


/**
 * A enum based config usage example.
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
 *  limitations under the License.
 */
public class ConfigEnumKeyValueExampleTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        System.getProperties().put("jConfigMap.location", "/home/dave/dev/jConfigMap/src/test/resources/config");
    }

    public void test__ONE() {

        String test = ConfigEnumExample.ONE.get(String.class);
        assertEquals("first value", test);
    }

    public void test__TWO() {

        int test = Integer.parseInt(ConfigEnumExample.TWO.get(String.class));
        assertEquals(1, test);

        assertEquals((Integer)1, ConfigEnumExample.TWO.get(Integer.class));
    }

    public void test__THREE() {

        assertEquals((Double)2.0, ConfigEnumExample.THREE.get((Double.class)));
    }

    public void test__FOUR() {
        assertTrue(ConfigEnumExample.FOUR.get(Boolean.class));
    }

    public void test__FIVE() {

        String testList = ConfigEnumExample.FIVE.get(String.class);
        assertEquals("AMD, INTC, WFMI, SCCO", testList);

        List<String> testValues = ConfigEnumExample.FIVE.get(List.class);
        assertEquals(4, testValues.size());
    }
}
