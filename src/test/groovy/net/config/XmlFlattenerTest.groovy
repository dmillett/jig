package net.config

import net.config.XmlFlattener

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 4/25/11
 * Time: 1:01 PM
 * To change this template use File | Settings | File Templates.
 */
class XmlFlattenerTest
    extends GroovyTestCase {


    void test__findSimpleKeyValueNodes() {

        def baseNode = new XmlParser().parse('/home/dave/dev/easy-config/data/ConfigOne.xml')
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

        def baseNode = new XmlParser().parse('/home/dave/dev/easy-config/data/ConfigOne.xml')
        def xmlFlatten = new XmlFlattener()

        def structureNode = baseNode.xmlStructure[0]
        Map keyValues = xmlFlatten.findXmlStructures(structureNode, "")

        assertNotNull(keyValues)
        assertEquals(10, keyValues.size())

        assertEquals("8.00", keyValues.get("xmlstructure.commission.type.stocks.ticker.intc.category.market"))

        keyValues.entrySet().each { entry ->
            println "$entry"
        }
    }
}
