package net.util;

import junit.framework.TestCase;

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
public class GenericsHelperTest
    extends TestCase {

    public void test__get_Integer() {

        GenericsHelper helper = new GenericsHelper();
        Integer value = helper.get("1", Integer.class);
        assertEquals(new Integer(1), value);
    }

    public void test__get_Integer_fail() {

        GenericsHelper helper = new GenericsHelper();
        Integer value = helper.get("A", Integer.class);
        assertNull(value);
    }

    // The others are tested in ConfigEnumKeyValueExample
}
