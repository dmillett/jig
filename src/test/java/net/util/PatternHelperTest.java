package net.util;

import junit.framework.TestCase;
import java.util.regex.Pattern;

/**
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
public class PatternHelperTest
    extends TestCase {

    public void test__buildPattern() {

        Pattern p1 = PatternHelper.buildPattern("stock");
        assertEquals("stock.*", p1.pattern());

        Pattern p2 = PatternHelper.buildPattern(true, "stock");
        assertEquals(".*stock.*", p2.pattern());
        
        Pattern p3 = PatternHelper.buildPattern("stock", "amd");
        assertEquals("stock.*amd.*", p3.pattern());

        Pattern p4 = PatternHelper.buildPattern("stock.amd");
        assertEquals("stock\\.amd.*", p4.pattern());
        
        Pattern p5 = PatternHelper.buildPattern(true, "stock.amd");
        assertEquals(".*stock\\.amd.*", p5.pattern());

        Pattern p6 = PatternHelper.buildPattern(true, "stock", "amd");
        assertEquals(".*stock.*amd.*", p6.pattern());
    }

    public void test_buildPattern_null() {

        // Silly java and its nulls with erasure ... almost.
        Pattern p1 = PatternHelper.buildPattern((String[])null);
        assertEquals(".*", p1.pattern());

        Pattern p2 = PatternHelper.buildPattern(true, (String[])null);
        assertEquals(".*", p2.pattern());
    }

    public void test__useFind_not() {

        assertFalse(PatternHelper.useFind(null));

        Pattern p1 = PatternHelper.buildPattern(true, "foo");
        assertFalse(PatternHelper.useFind(p1));
    }

    public void test__useFind() {

        Pattern p1 = PatternHelper.buildPattern("foo");
        assertTrue(PatternHelper.useFind(p1));

        Pattern p2 = PatternHelper.buildPattern("foo.*");
        assertTrue(PatternHelper.useFind(p2));
    }

    public void test__useContains_not() {

        assertFalse(PatternHelper.useContains(null));

        Pattern p1 = PatternHelper.buildPattern("foo");
        assertFalse(PatternHelper.useContains(p1));

        Pattern p2 = PatternHelper.buildPattern("foo", "bar");
        assertFalse(PatternHelper.useContains(p2));

        Pattern p3 = Pattern.compile("foo+");
        assertFalse(PatternHelper.useContains(p3));

        Pattern p4 = PatternHelper.buildPattern("stock.amd");
        assertFalse(PatternHelper.useContains(p4));
    }

    public void test__useContains() {

        Pattern p1 = Pattern.compile("foo");
        assertTrue(PatternHelper.useContains(p1));
    }
}
