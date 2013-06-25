package net.util;


import junit.framework.TestCase;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class TextTrieMapTest
    extends TestCase {

    public void test__simple_add() {

        TextTrieMap trieMap = buildTrieMapOne();

        assertNotNull(trieMap);
        assertEquals(2, trieMap.getNodes().size());

        // "a", "b" are the top level nodes
        for ( TextTrieMap.TextNode node : trieMap.getNodes() )
        {
            if ( "a".equals(node.getKey()) )
            {
                assertEquals(2, node.getChildNodes().size());
            }
            else
            {
                assertEquals(1, node.getChildNodes().size());
            }
        }
    }

    public void test__simple_add2() {

        TextTrieMap trieMap = buildTextTrieMap2();

        assertNotNull(trieMap);
        assertEquals(2, trieMap.getNodes().size());

        // "a", "b" are the top level nodes
        for ( TextTrieMap.TextNode node : trieMap.getNodes() )
        {
            if ( "a".equals(node.getKey()) )
            {
                assertEquals(2, node.getChildNodes().size());

                // a.c -> a.c.e, a.b
                for ( TextTrieMap.TextNode childNode : node.getChildNodes() )
                {
                    if ( "a.c".equals(childNode.getKey()) )
                    {
                        assertEquals(1, childNode.getChildNodes().size());
                    }
                    else
                    {
                        assertEquals("a.b", childNode.getKey());
                        assertEquals(0, childNode.getChildNodes().size());
                    }
                }
            }
            else
            {
                // "b", "b.d"
                assertEquals("b", node.getKey());
                assertEquals(1, node.getChildNodes().size());
            }
        }
    }

    public void test__getNodesWithPrefix() {

        TextTrieMap trieMap = buildTextTrieMap2();

        Set<TextTrieMap.TextNode> anodes = trieMap.getNodesWithPrefix("a");
        assertEquals(4, anodes.size());

        Set<TextTrieMap.TextNode> bnodes = trieMap.getNodesWithPrefix("b");
        assertEquals(2, bnodes.size());

        Set<TextTrieMap.TextNode> abnode = trieMap.getNodesWithPrefix("a.b");
        assertEquals(1, abnode.size());
    }

    public void test__getAllNodes() {

        TextTrieMap trieMap = buildTextTrieMap2();
        assertEquals(6, trieMap.getAllNodes().size());
    }

    private TextTrieMap buildTrieMapOne() {

        String one = "a";
        String two = "a.b";
        String three = "b";
        String four = "b.d";
        String five = "a.c";

        TextTrieMap trieMap = new TextTrieMap();
        trieMap.addNode(one, "one");
        trieMap.addNode(two, "two");
        trieMap.addNode(three, "three");
        trieMap.addNode(four, "four");
        trieMap.addNode(five, "five");

        return trieMap;
    }

    private TextTrieMap buildTextTrieMap2() {

        String one = "a";
        String two = "a.b";
        String three = "b.d";
        String four = "b";
        String five = "a.c.e";
        String six = "a.c";

        TextTrieMap trieMap = new TextTrieMap();
        trieMap.addNode(one, "one");
        trieMap.addNode(two, "two");
        trieMap.addNode(three, "three");
        trieMap.addNode(four, "four");
        trieMap.addNode(five, "five");
        trieMap.addNode(six, "six");

        return trieMap;
    }

    public void test__isSibling() {

        TextTrieMap.TextNode tn = new TextTrieMap.TextNode("foo.bar", "foo bar");
        TextTrieMap.TextNode tn1 = new TextTrieMap.TextNode("foo.bar.zoo.x", "foo bar zoo x");
        TextTrieMap.TextNode tn2 = new TextTrieMap.TextNode("foo.bar.zoo.y", "foo bar zoo y");

        assertFalse(tn.isSibling(tn1));
        assertTrue(tn1.isSibling(tn2));
    }

    public void test__isChild() {

        TextTrieMap.TextNode tn = new TextTrieMap.TextNode("how.do.you.do", "how do you do");
        TextTrieMap.TextNode tn1 = new TextTrieMap.TextNode("how.do.you", "how do you");
        TextTrieMap.TextNode tn2 = new TextTrieMap.TextNode("how.do.i.know.you", "how do i know you");

        assertFalse(tn1.isChild(tn));
        assertFalse(tn1.isChild(tn2));

        assertTrue(tn.isChild(tn1));
    }

    public void test__put_moderately_complex() {

        Map<String, String> original = buildModeratelyComplexMap();
        TextTrieMap textTrie = new TextTrieMap();

        for ( Map.Entry<String, String> entry : original.entrySet() )
        {
            textTrie.put(entry.getKey(), entry.getValue());
        }

        assertEquals(6, textTrie.size());

        TextTrieMap.TextNode node = textTrie.getNode("structures.foo.some.other.value");

        assertNotNull(node);
        assertTrue(node.hasChildren());
        assertEquals(2, node.getChildNodes().size());
    }

    private Map<String, String> buildModeratelyComplexMap() {

        String base1 = "structures.foo";
        String base2 = "structures.bar";

        Map<String, String> m = new HashMap<String, String>();

        m.put(base1 + ".some.single.value", "foo some single value");
        m.put(base1 + ".some.other.value", "foo some other value");
        m.put(base1 + ".other.value", "foo other value");
        m.put(base1 + ".some.other.value.named.x", "foo some other value named x");
        m.put(base1 + ".some.other.value.named.y", "foo some other value named y");

        m.put(base2 + ".some.random.value", "bar some random value");

        return m;
    }

}
