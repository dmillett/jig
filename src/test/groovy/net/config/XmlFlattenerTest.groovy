package net.config

/**
 *
 */
class XmlFlattenerTest
    extends GroovyTestCase {

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
        assertEquals(12, keyValues.size())

        assertEquals("8.00", keyValues.get("xmlstructure.commission.type.stocks.ticker.intc.category.market"))

        keyValues.entrySet().each { entry ->
            println "$entry"
        }
    }
}
