package net.config

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
class JsonFlattenerTest
    extends GroovyTestCase {

    // todo: fix hardcoded files ... investigate url options
    void test__flattenJsonFile() {

        def fileUrl = "file:///home/dave/dev/jConfigMap/src/test/resources/config/JsonExampleOne.json"
        def jsonFlattener = new JsonFlattener()
        def exampleThreeKeyValues = jsonFlattener.flattenJsonFile(fileUrl)

        assertNotNull(exampleThreeKeyValues)
        assertEquals(9, exampleThreeKeyValues.size())
    }

    void test__flattenJsonFile_simple2() {

        def fileUrl = "file:///home/dave/dev/jConfigMap/src/test/resources/config/JsonExampleTwo.json"
        def jsonFlattener = new JsonFlattener()
        def simpleJsonTwo = jsonFlattener.flattenJsonFile(fileUrl)

        assertNotNull(simpleJsonTwo)
        assertEquals(18, simpleJsonTwo.size())
    }

    void test__flattenJsonFile_simple3() {

        def fileUrl = "file:///home/dave/dev/jConfigMap/src/test/resources/config/JsonExampleThree.json"
        def jsonFlattener = new JsonFlattener()
        def simpleJsonThree = jsonFlattener.flattenJsonFile(fileUrl)

        assertNotNull(simpleJsonThree)
        assertEquals(26, simpleJsonThree.size())
    }

    void test__flattenJson_complex1() {

        def fileUrl = "file:///home/dave/dev/jConfigMap/src/test/resources/config/JsonExampleFour.json"
        def jsonFlattener = new JsonFlattener()
        def simpleJsonFour = jsonFlattener.flattenJsonFile(fileUrl)

        assertNotNull(simpleJsonFour)
    }
}
