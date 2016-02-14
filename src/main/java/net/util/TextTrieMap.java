package net.util;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A TrieMap storing TextTrieMap.TextNode, composed of 'key:value' pairs
 * based on String prefix. This is meant to increase performance for group
 * retrieval based on key prefix. Lookup performance is based on the
 * specified prefix precision and the number of 'matching nodes' vs top
 * level nodes (non-matching). In this case, a node will only have a child
 * node if the current node is a prefix of the child node.
 *
 * Worst case is O(n) since there is no guarantee that data will have
 * a shared prefix. The performance gets better as the frequency
 * of grouped config increases.
 *
 * Another improvement to average case would be to identify ancestors
 * via their dot delimiters.
 *
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
 * Not thread safe
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
public class TextTrieMap
    implements Map<String,String> {

    /** Store all of the nodes here */
    private final Set<TextNode> _nodes;

    /** Determined by addNodeRecursive() */
    private int _size;

    /** Used in conjunction with _allNodes and _lastCalculated, should guarantee correctness */
    private long _lastUpdated;

    private long _lastCalculated;
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

        boolean exists = false;
        boolean moveChildren = false;

        for ( TextNode textNode : nodes )
        {
            if ( textNode.isParent(node) )
            {
                addNodeRecursive(textNode.getChildNodes(), node);
                exists = true;
                break;
            }
            else if ( textNode.isChild(node) )
            {
                nodes.add(node);
                exists = true;
                moveChildren = true;
                _size++;
                break;
            }
        }

        if ( moveChildren )
        {
            moveChildrenToNode(node, nodes);
        }

        if ( !exists )
        {
            nodes.add(node);
            _size++;
        }

        _lastUpdated = System.nanoTime();
    }

    private void moveChildrenToNode(TextNode node, Set<TextNode> nodes) {

        List<TextNode> newChildNodes = new LinkedList<TextNode>();
        Iterator<TextNode> textNodes = nodes.iterator();

        while ( textNodes.hasNext() )
        {
            TextNode textNode = textNodes.next();

            if ( !node.equals(textNode) && textNode.isChild(node) )
            {
                newChildNodes.add(textNode);
                textNodes.remove();
            }
        }

        for ( TextNode childNode : newChildNodes )
        {
            node.addChild(childNode);
        }
    }

    /**
     * Adds a node recursively
     * @param key non-null
     * @param value any text value
     */
    public void addNode(String key, String value) {

        if ( key == null ) { return; }

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

        if ( key == null ) { return null; }

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
     * @return All of the TextNode for '_nodes'.
     */
    public Set<TextNode> getAllNodes() {

        if ( _allNodes == null || _allNodes.isEmpty() || _lastCalculated < _lastUpdated )
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
                allNodes.addAll(getAllNodes(node.getChildNodes()));
            }
        }

        return allNodes;
    }

    @Override
    public int size() {
        return getAllNodes().size();
    }

    @Override
    public boolean isEmpty() {
        return getAllNodes().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getNode((String)key) != null;
    }

    @Override
    public boolean containsValue(Object value) {

        String textValue = (String)value;
        boolean nullValue = textValue == null;

        for ( TextNode node : _nodes )
        {
            if ( !nullValue && textValue.equals(node._value) )
            {
                return true;
            }
            else if ( nullValue && node._value == null )
            {
                return true;
            }
        }

        return false;
    }

    @Override
    public String get(Object key) {
        return getNode((String)key)._value;
    }

    @Override
    public String remove(Object key) {
        return null;  //Todo
    }

    @Override
    public void clear() {
        _nodes.clear();
    }

    @Override
    public Set<String> keySet() {

        Set<String> keys = new HashSet<String>(_nodes.size());
        for ( TextNode node : _nodes )
        {
            keys.add(node.getKey());
        }

        return keys;
    }

    @Override
    public Collection<String> values() {

        List<String> values = new ArrayList<String>(_nodes.size());
        for ( TextNode node : _nodes )
        {
            values.add(node._value);
        }

        return values;
    }

    @Override
    public Set<Entry<String, String>> entrySet() {

        Set<Entry<String,String>> entries = new HashSet<Entry<String, String>>(_nodes.size());

        for ( TextNode node : _nodes )
        {
            entries.add(new TtEntry(node));
        }

        return entries;
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> m) {

        if ( m == null )
        {
            return;
        }

        for ( Entry<? extends String, ? extends String> entry : m.entrySet() )
        {
            addNode(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String put(String key, String value) {

        addNode(key, value);
        return null;
    }

    // Explore immutability -- breaks setValue();
    static class TtEntry
            implements Entry<String, String> {

        private String _ttKey;
        private String _ttValue;

        TtEntry(TextNode textNode) {
            _ttKey = textNode.getKey();
            _ttValue = textNode.getValue();
        }

        TtEntry(String key, String value) {
            _ttKey = key;
            _ttValue = value;
        }

        @Override
        public String getKey() {
            return _ttKey;
        }

        @Override
        public String getValue() {
            return _ttValue;
        }

        @Override
        public String setValue(String value) {

            String oldValue = _ttValue;
            _ttValue = value;
            return oldValue;
        }
    }


    /**
     * A simple key value store. Null keys are not allowed.
     */
    public static final class TextNode {

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

        public TextNode(TextNode textNode) {

            if ( textNode == null || textNode._key == null )
            {
                throw new NullPointerException("TextNode Key Cannot Be Null");
            }

            _key = textNode._key;
            _value = textNode._value;
            _childNodes = new HashSet<TextNode>(textNode._childNodes);
        }

        public String getKey() {
            return _key;
        }

        public String getValue() {
            return _value;
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

        public boolean isSibling(TextNode node) {

            if ( node == null )
            {
                return false;
            }

            return isSibling(node.getKey());
        }

        public boolean isSibling(String key) {

            if ( key == null )
            {
                return false;
            }

            String currentBase = _key.substring(0, _key.lastIndexOf('.'));
            String base = key.substring(0, key.lastIndexOf('.'));

            return currentBase.equals(base);
        }

        /** Contains the complete key of this node and then some */
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

        public boolean isSameKey(TextNode node) {

            if ( node == null )
            {
                return false;
            }

            return _key.equals(node._key);
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

