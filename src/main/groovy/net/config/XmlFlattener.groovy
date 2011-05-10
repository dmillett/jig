package net.config

import org.apache.log4j.Logger

/**
 * @author dmillett
 */
class XmlFlattener {

    private static final def Logger LOG = Logger.getLogger("XmlFlattener")
    private static final def String DELIM = "."

    /**
     * This creates a map from the following two XML nodes in 'configFile'. It looks for the following
     * two node names:
     * 'keyValueProperties'
     * 'structureXml'
     *
     * @see 'findSimpleKeyValueNodes()'
     * @see 'findXmlStructures()'
     *
     * @param configFile The xml configuration file
     * @return A Map (hash) of all the config values for 'keyValueProperties' and 'structureXml'
     */
    def Map<String, String> flatten(String configFile) {

        LOG.info("Loading Xml File To Flatten To Map $configFile")

        def currentDir = new File('').absolutePath
        def configNode = new XmlParser().parse(configFile)

        Map<String,String> keyValues = findSimpleKeyValueNodes(configNode)
        keyValues.putAll(findXmlStructures(configNode.xmlStructure[0], ""))

        return keyValues
    }

    /**
     * Looks for a specific structure in the configuration to support a simple
     * key value structure within xml.
     *
     * <pre>
     * <config>
     *   <keyValueProperties>
     *     <property name="some.prop" value="1.5" />
     *     <property name="foo" value="bar" />
     *     <property name="active">true</property>
     *   </keyValueProperties>
     * </config>
     * </pre>
     *
     * @param baseNode
     * @return A Hashmap with String keys ('name') and values ('value' or text())
     */
    def Map<String, String> findSimpleKeyValueNodes(Node baseNode) {

        LOG.info("Loading Simple Key Values")
        Map<String, String> keyValues = new HashMap<String, String>()

        baseNode.keyValueProperties.property.each { entry ->
            keyValues.put(entry.@name, checkNodeForValue(entry))
        }

        keyValues
    }

    /**
     * Check the current xml node for any "text" value or
     * the 'value' attribute (@value). Prefer the 'text' value
     * over the attribute ('value'). Note that Groovy treats
     * node.text() as '' instead of null if no value is present.
     *
     * @param node
     * @return
     */
    def String checkNodeForValue(Node node) {

        if ( node == null )
        {
            return null
        }

        if ( !node.text().empty && node.@value == null )
        {
            return node.text()
        }
        else if ( !node.text().empty && node.@value != null )
        {
            LOG.info("Using Node text() value: '${node.text()}', Instead Of Attribute @value: '${node.@value}'")
            return node.@value
        }
        else if ( node.@value != null && node.text().empty )
        {
            return node.@value
        }

        return null
    }


    /**
     * Look at the attributes for a given Node and only take the "value"
     * for each attribute. It gets chained into a flattened form.
     *
     * <pre>
     * <node name="foo" description="bar>
     *   <subNode key="really" value="no shite"/>
     * </node>
     * </pre>
     *
     * Flattens into "foo.bar.really","no shite"
     *
     * @param attributes
     * @return
     */
    def String flattenNodeName(Node node) {

        if ( node == null )
        {
            return null
        }

        def name = node.name()
        node.attributes().entrySet().each { entry ->

            name += "." + entry.getValue()
        }

        return name
    }

    /**
     * To flatten or not to flatten. This works for:
     *
     * stock.AMD.sell-high, 25.00
     * stock.AMD.sell-low, 6.50
     * stock.AMD.shares, 200
     *
     * <pre>
     *  <stock name="AMD">
     *    <sell-high>25.00</sell-high>
     *    <sell-low>6.50</sell-low>
     *    <shares>200</shares>
     *  </stock>
     * </pre>
     *
     * @param baseNode
     * @return
     */
    def Map<String, String> findXmlStructures(Node baseNode, String name) {

        LOG.info("Loading Structured Xml Flattened Key Values")

        def currentName = ""
        def flattenedNodeName = flattenNodeName(baseNode)

        if ( name == null || name.empty )
        {
            currentName = flattenedNodeName
        }
        else
        {
            currentName = name + "." + flattenedNodeName
        }

        Map keyValues = new HashMap<String, String>()

        baseNode.children().each { childNode ->

            def childNodeValue = checkNodeForValue(childNode)

            if ( childNodeValue != null )
            {
                def childNodeName = flattenNodeName(childNode)
                def key = currentName + "." + childNodeName
                keyValues.put(key, childNodeValue)
                return
            }

            keyValues.putAll(findXmlStructures(childNode, currentName))
        }

        return keyValues
    }
}
