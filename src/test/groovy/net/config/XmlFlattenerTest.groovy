package net.config

/**
 * Testing the xml structure and flattening of nodes.
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
class XmlFlattenerTest
    extends GroovyTestCase {

    void test__isValidConfigFile() {

        def flattener = new XmlFlattener()

        String xmlOne = '''<config></config>'''
        assertFalse(flattener.isValidConfigFile(new XmlParser().parseText(xmlOne)))

        String validXmlOne = '''<config><keyValueProperties></keyValueProperties></config>'''
        assertTrue(flattener.isValidConfigFile(new XmlParser().parseText(validXmlOne)))

        String validXmlTwo = '''<config><xmlStructure></xmlStructure></config>'''
        assertTrue(flattener.isValidConfigFile(new XmlParser().parseText(validXmlTwo)))
    }

    void test__flatten_IgnoredConfigFile() {

        def ignoredConfigFile = GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "IgnoredConfig.xml"
        def xmlFlatten = new XmlFlattener()
        
        xmlFlatten.flatten(ignoredConfigFile)
    }

    void test__findSimpleKeyValueNodes() {

        def testConfigFile = GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "ConfigOne.xml"
        def baseNode = new XmlParser().parse(testConfigFile)

        def xmlFlatten = new XmlFlattener()
        def keyValues = xmlFlatten.findSimpleKeyValueNodes(baseNode)

        assertNotNull(keyValues)
        assertEquals(5, keyValues.size())

        assertEquals("first value", keyValues.get("key.one.string"))
        assertEquals("1", keyValues.get("key.two.int"))
        assertEquals("2.0", keyValues.get("key.three.double"))
        assertTrue(Boolean.valueOf(keyValues.get("key.four.boolean")))
        assertEquals("AMD, INTC, WFMI, SCCO", keyValues.get("key.five.list"))
    }

    void test__findXmlStructures() {

        def testConfigFile = GroovyTestConfigHelper.updateSystemPropertyConfigLocation() + "ConfigOne.xml"
        def baseNode = new XmlParser().parse(testConfigFile)
        def xmlFlatten = new XmlFlattener()

        def structureNode = baseNode.xmlStructure[0]
        def keyValues = xmlFlatten.findXmlStructures(structureNode, "")

        assertNotNull(keyValues)
        assertEquals(14, keyValues.size())

        assertEquals("8.00", keyValues.get("xmlstructure.commission.type.stocks.ticker.intc.category.market"))
    }
}
