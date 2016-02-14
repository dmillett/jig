package net.config

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class flattens xml structures and their values into
 * a HashMap. Each config file must have the following form:
 *
 * <pre>
 * <!-- required root node -->
 * <config>
 *   <keyValues>
 *       <!-- Define optional nodes here -->
 *   </keyValues>
 *   <structures>
 *      <!-- Define optional nodes here -->
 *   </structures>
 * </config>
 * </pre>
 *
 * Where the root node 'config' is required and one or both
 * of the child nodes 'keyValues' and 'structures'
 * are required.
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
class XmlFlattener {

    private static final def Logger LOG = LoggerFactory.getLogger(XmlFlattener.class)
    private static final def String DELIM = "."

    /**
     * This creates a map from the following two XML child nodes in 'configFile'.
     * It requires the root node and one or both of the child nodes below. If the
     * xml is not well formed, then XmlParser throws an exception and the file is
     * ignored.
     *
     * root node: 'config'
     *
     * child node(s):
     * 'keyValues'
     * 'structures'
     *
     * @see 'findSimpleKeyValueNodes()'
     * @see 'findXmlStructures()'
     *
     * @param configFile The xml configuration file
     * @return A Map (hash) of all the config values for 'keyValues' and 'structures'
     */
    def Map<String, String> flatten(String configFile) {

        LOG.info("Loading Xml File To Flatten To Map $configFile")
        def keyValues

        try
        {
            def configNode = new XmlParser().parse(configFile)
            if ( !isValidConfigFile(configNode) )
            {
                LOG.info("Skipping ${configFile} Due To Improper Config Structure")
                return new HashMap<String,String>()
            }

            keyValues = findSimpleKeyValueNodes(configNode)
            keyValues.putAll(findXmlStructures(configNode.structures[0], ""))
        }
        catch ( Throwable t )
        {
            LOG.error("Could Not Parse ${configFile} Due To", t)
        }

        return keyValues
    }

    /**
     * Looks for a specific structure in the configuration to support a simple
     * key value structure within xml.
     *
     * <pre>
     * <config>
     *   <keyValues>
     *     <property name="some.prop" value="1.5" />
     *     <property name="foo" value="bar" />
     *     <property name="active">true</property>
     *   </keyValues>
     * </config>
     * </pre>
     *
     * @param baseNode
     * @return A Hashmap with String keys ('name') and values ('value' or text())
     */
    def Map<String, String> findSimpleKeyValueNodes(Node baseNode) {

        LOG.info("Loading Simple Key Values")
        def properties = new HashMap<String, String>()

        baseNode.keyValues.property.each { entry ->
            properties.put(entry.@name.toLowerCase(), checkNodeForValue(entry))
        }

        properties
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
     * <pre>
     * some.not.so.unique.key,0 "foo"
     * some.not.so.unique.key.1, "bar"
     * some.not.so.unique.key.2, "sh!t"
     * </pre>
     *
     * The config retrieval already returns a Map, this forces the user to
     * deal with the possible results instead of overwriting them.
     *
     * <pre>
     *
     * structures.stock.AMD.sell-high, 25.00
     * structures.stock.AMD.sell-low, 6.50
     * structures.stock.AMD.shares, 200
     *
     * <config>
     *   <structures>
     *      <stock name="AMD">
     *        <sell-high>25.00</sell-high>
     *        <sell-low>6.50</sell-low>
     *        <shares>200</shares>
     *      </stock>
     *   </structures>
     * </config>
     *
     * </pre>
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
     * and either/both of "keyValue" or "structures" child
     * nodes.
     *
     * @param configBaseNode
     * @return
     */
    def boolean isValidConfigFile(Node configBaseNode) {

        if ( configBaseNode == null || !configBaseNode.name().equals("config") )
        {
            LOG.warn("Skipping: Invalid Configuration Structure For Root Node: ${configBaseNode}")
            return false
        }

        if ( configBaseNode.keyValues[0] == null && configBaseNode.structures[0] == null )
        {
            LOG.info("Skipping: Missing 'keyValues' Or 'structures' Node(s)")
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
     *   <structures>
     *     <node name="foo" description="bar>
     *       <subNode key="really" value="no shite"/>
     *     </node>
     *   </structures>
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
