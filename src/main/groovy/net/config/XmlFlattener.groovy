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

        def configNode = new XmlParser().parse(configFile)
        if ( !validConfigFile(configNode) )
        {
            return new HashMap<String,String>()
        }

        def keyValues = findSimpleKeyValueNodes(configNode)
        println "xmlStructure? ${configNode.xmlStructure[0]}"
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
        def keyValues = new HashMap<String, String>()

        baseNode.keyValueProperties.property.each { entry ->
            keyValues.put(entry.@name.toLowerCase(), checkNodeForValue(entry))
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
     * To flatten or not to flatten. This recursive method works for the structure
     * below. If there are any identical key node names, then the 2 - N identical
     * keys encountered will automatically append the count in the load order.
     *
     * some.not.so.unique.key, "foo"
     * some.not.so.unique.key.1, "bar"
     * some.not.so.unique.key.2, "sh!t"
     *
     * The config retrieval already returns a Map, this forces the user to
     * deal with the possible results instead of overwriting them.
     *
     * stock.AMD.sell-high, 25.00
     * stock.AMD.sell-low, 6.50
     * stock.AMD.shares, 200
     * <config>
     *   <xmlStructure>
     *     <pre>
     *      <stock name="AMD">
     *        <sell-high>25.00</sell-high>
     *        <sell-low>6.50</sell-low>
     *        <shares>200</shares>
     *      </stock>
     *     </pre>
     *
     *   </xmlStructure>
     * </config>
     *
     * @param baseNode
     * @return
     */
    def Map<String, String> findXmlStructures(Node baseNode, String name) {

        LOG.info("Loading Structured Xml Flattened Key Values")
        def currentName = findCurrentFlattenedName(name, baseNode)
        def duplicateKeyCount = 1
        def keyValues = new HashMap<String, String>()

        baseNode.children().each { childNode ->

            def childNodeValue = checkNodeForValue(childNode)

            if ( childNodeValue != null )
            {
                def lowerCaseKey = buildKeyForChildNode(currentName, childNode)

                if ( keyValues.containsKey(lowerCaseKey) )
                {
                    def keyWithIndex = lowerCaseKey + DELIM + duplicateKeyCount
                    keyValues.put(keyWithIndex, childNodeValue)
                    duplicateKeyCount++
                }
                else
                {
                    keyValues.put(lowerCaseKey, childNodeValue)
                }

                return
            }

            // Recursive call (some day it will be tail recursion)
            keyValues.putAll(findXmlStructures(childNode, currentName))
        }

        return keyValues
    }

    /**
     * A valid jConfigMap file must have a "config" root node
     * and either/both of "keyValueProperties" or "xmlStructure" child
     * nodes.
     *
     * @param configBaseNode
     * @return
     */
    def boolean validConfigFile(Node configBaseNode) {

        if ( configBaseNode == null || !configBaseNode.name().equals("config") )
        {
            LOG.warn("Invalid Configuration Structure For Root Node: ${configBaseNode}")
            return false
        }

        println "${configBaseNode.keyValueProperties.dump()}"

        if ( configBaseNode.keyValueProperties[0] == null && configBaseNode.xmlStructure[0] == null )
        {
            return false
        }

        return true
    }

    /**
     * Look at the attributes for a given Node and only take the "value"
     * for each attribute. It gets chained into a flattened form.
     *
     * <pre>
     * <config>
     *   <xmlStructure>
     *     <node name="foo" description="bar>
     *       <subNode key="really" value="no shite"/>
     *     </node>
     *   </xmlStructure>
     * </config>
     * </pre>
     *
     * Flattens into "foo.bar.really","no shite"
     *
     * @param attributes
     * @return
     */
    private def String flattenNodeName(Node node) {

        if ( node == null )
        {
            return null
        }

        def name = node.name()
        node.attributes().entrySet().each { entry ->
            name += DELIM + entry.getValue()
        }

        return name
    }

    // Determine the current name from the current node and base name value
    private def String findCurrentFlattenedName(String name, Node node) {

        def currentName = ""
        def flattenedNodeName = flattenNodeName(node)

        if ( name == null || name.empty )
        {
            currentName = flattenedNodeName
        }
        else
        {
            currentName = name + DELIM + flattenedNodeName
        }

        return currentName
    }

    // Append node attributes as names to the current name
    private def String buildKeyForChildNode(String currentName, Node childNode) {

        def childNodeName = flattenNodeName(childNode)
        return (currentName + DELIM + childNodeName).toLowerCase()
    }
}
