package net.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A TrieMap storing TextTrieMap.TextNode, composed of 'key:value' pairs
 * based on String prefix. This is meant to increase performance for group
 * retrieval based on key prefix. Lookup performance is based on the
 * specified prefix precision and the number of 'matching nodes' vs top
 * level nodes (non-matching). At some point I'll try and write the O notation
 * here.
 *
 * a
 * | a.b
 * | a.c
 *    | a.c.e
 * b
 * | b.e
 *    | b.e.foo
 * c
 *
 */
public class TextTrieMap
    implements Map<String, String> {

    /** Store all of the nodes here */
    private final Set<TextNode> _nodes;

    /** Determined by addNodeRecursive() */
    private int _size;
    // Memory vs lookup speed
    transient volatile Set<TextNode> _allNodes;


    /** An empty Set of TextNode  */
    public TextTrieMap() {
        _nodes = new HashSet<TextNode>();
    }

    public Set<TextNode> getNodes() {
        return _nodes;
    }

    /** A recursive addition of a TextNode */
    public void addNode(TextNode node) {
        addNodeRecursive(_nodes, node);
    }

    /**
     * Recursively add nodes
     * @param nodes
     * @param node
     */
    protected void addNodeRecursive(Set<TextNode> nodes, TextNode node) {


        if ( node == null || node._key == null )
        {
            return;
        }

        boolean added = false;
        for ( TextNode textNode : nodes )
        {
            if ( textNode.isParent(node) )
            {
                addNodeRecursive(textNode._childNodes, node);
                added = true;
                break;
            }
            else if ( textNode.isChild(node) )
            {
                node.addChild(textNode);
                nodes.add(node);
                added = true;
                nodes.remove(textNode);
                _size++;
                break;
            }
        }

        if ( !added )
        {
            nodes.add(node);
            _size++;
        }
    }

    /**
     * Adds a node recursively
     * @param key non-null
     * @param value any text value
     */
    public void addNode(String key, String value) {

        if ( key == null )
        {
            return;
        }

        TextNode node = new TextNode(key, value);
        addNode(node);
    }

    /**
     * Get exact or first closes. This iterates over all of
     * the child nodes, but looks at depth first.
     * @param key a non-null key
     * @return The text node with matching 'key' or 'null'
     */
    public TextNode getNode(String key) {

        if ( key == null )
        {
            return null;
        }

        for ( TextNode node : _nodes )
        {
            if ( node._key.equals(key) )
            {
                return node;
            }
            else if ( node._key.startsWith(key) )
            {
                return getNode(key);
            }
        }

        return null;
    }

    public Set<TextNode> getNodesWithPrefix(String prefix) {
        return getNodesWithPrefix(_nodes, prefix);
    }

    // Grab any node where 'startsWith(prefix)' is true
    protected Set<TextNode> getNodesWithPrefix(Set<TextNode> nodes, String prefix) {

        if ( prefix == null )
        {
            return new HashSet<TextNode>();
        }

        return depthTraversal(nodes, prefix);
    }

    /**
     * Traverse depth into each node searching for prefix 'matches'
     * @param nodes TextNodes
     * @param prefix A non-null prefix for each text node 'key'
     * @return A set of textnodes that start with 'prefix'
     */
    protected Set<TextNode> depthTraversal(Set<TextNode> nodes, String prefix) {

        if ( prefix == null || nodes.isEmpty() )
        {
            return new HashSet<TextNode>(0);
        }

        Set<TextNode> depthNodes = new HashSet<TextNode>();

        for ( TextNode node : nodes )
        {
            if ( prefix.startsWith(node._key) )
            {
                depthNodes.addAll(depthTraversal(node._childNodes, prefix));
            }

            if ( node._key.startsWith(prefix) )
            {
                depthNodes.add(node);
                depthNodes.addAll(getAllNodes(node._childNodes));
                break;
            }
        }

        return depthNodes;
    }

    /**
     * @return All of the TextNode for '_nodes'
     */
    public Set<TextNode> getAllNodes() {

        if ( _allNodes == null || _allNodes.isEmpty() )
        {
            _allNodes = getAllNodes(_nodes);
        }

        return _allNodes;
    }

    /**
     * Get all nodes from a given Set of nodes. This will recursively call
     * into each node that has children.
     *
     * @param nodes A given set of nodes
     * @return A Set of all the nodes for a given set of nodes
     */
    protected Set<TextNode> getAllNodes(Set<TextNode> nodes) {

        Set<TextNode> allNodes = new HashSet<TextNode>();

        for ( TextNode node : nodes )
        {
            allNodes.add(node);

            if ( node.hasChildren() )
            {
                allNodes.addAll(getAllNodes(node._childNodes));
            }
        }

        return allNodes;
    }

    // *************************** Map Implementations ********************
    public int size() {
        return _size;
    }

    public boolean isEmpty() {
        return _size < 1;
    }

    /** Uses getNode(String) and returns true if 'not-null' */
    public boolean containsKey(Object key) {

        if ( key == null ) { return false; }

        return getNode((String)key) != null;
    }

    /** Difficult, maybe multiple identical values, requires potentially full traversal  */
    public boolean containsValue(Object value) {

        String textValue = (String)value;

        for ( TextNode node : getAllNodes() )
        {
            if ( node._value != null && node._value.equals(textValue) )
            {
                return true;
            }
        }

        return false;
    }

    public String get(Object key) {

        TextNode node = getNode((String)key);
        if ( node != null )
        {
            return node.getKey();
        }

        return null;
    }

    public String put(String key, String value) {

        return null;
    }

    public String remove(Object key) {
        return null;
    }

    public void putAll(Map<? extends String, ? extends String> m) {
    }

    public void clear() {
        _nodes.clear();
        _allNodes.clear();
    }

    public Set<String> keySet() {

        return null;
    }

    public Collection<String> values() {
        return null;
    }

    public Set<Entry<String, String>> entrySet() {
        return null;
    }

    /**
     * A simple key value store. Null keys are not allowed.
     */
    public static class TextNode
        implements Entry<String, String> {

        private final String _key;
        private final String _value;
        private final Set<TextNode> _childNodes;

        public TextNode(String key, String value) {

            if ( key == null )
            {
                throw new NullPointerException("TextNode Key Cannot Be Null");
            }

            _key = key;
            _value = value;
            _childNodes = new HashSet<TextNode>();
        }

        public String getKey() {
            return _key;
        }

        public String getValue() {
            return _value;
        }

        @Override
        public String setValue(String value) {
            return null;
        }

        public Set<TextNode> getChildNodes() {
            return _childNodes;
        }

        public void addChild(TextNode node) {

            if ( node != null )
            {
                _childNodes.add(node);
            }
        }

        public boolean isChild(TextNode node) {

            if ( node == null )
            {
                return false;
            }

            return isChild(node.getKey());
        }

        public boolean isChild(String keyPrefix) {

            if ( keyPrefix == null )
            {
                return false;
            }

            return _key.startsWith(keyPrefix);
        }

        /**
         * If this TextNode starts with the 'key' of node
         * @param node
         * @return
         */
        public boolean isParent(TextNode node) {

            if ( node == null )
            {
                return false;
            }

            return isParent(node.getKey());
        }

        /** make this node a child of 'keyPrefix'  */
        public boolean isParent(String keyPrefix) {

            if ( keyPrefix == null )
            {
                return false;
            }

            return keyPrefix.startsWith(_key);
        }

        public boolean hasChildren() {
            return _childNodes.size() > 0;
        }

        @Override
        public String toString() {
            return "TextNode{" +
                    "_key='" + _key + '\'' +
                    ", _value='" + _value + '\'' +
                    ", _childNodes=" + _childNodes +
                    '}';
        }

        @Override
        public boolean equals(Object o) {

            if ( this == o )
            {
                return true;
            }
            if ( o == null || getClass() != o.getClass() )
            {
                return false;
            }

            TextNode textNode = (TextNode) o;

            if ( !_key.equals(textNode._key) )
            {
                return false;
            }
            if ( _value != null ? !_value.equals(textNode._value) : textNode._value != null )
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {

            int result = _key.hashCode();
            result = 31 * result + (_value != null ? _value.hashCode() : 0);
            return result;
        }
    }
}

