package net.util;


import junit.framework.TestCase;
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

    // ************ test Map ********************************************

    public void test__map_size() {

        TextTrieMap trieMap5 = buildTrieMapOne();
        assertEquals(5, trieMap5.size());

        TextTrieMap trieMap6 = buildTextTrieMap2();
        assertEquals(6, trieMap6.size());
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
}
