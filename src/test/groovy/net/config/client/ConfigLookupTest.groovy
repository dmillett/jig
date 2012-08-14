package net.config.client

/**
 *
 */
class ConfigLookupTest
    extends GroovyTestCase {

    void test__buildPattern_null() {

        def cfgLookup = new ConfigLookup();
        assertNull(cfgLookup.buildPattern(null))
        assertNull(cfgLookup.buildPattern(""))
    }

    void test__buildPattern() {

        def cfgLookup = new ConfigLookup()
        def fooPattern = cfgLookup.buildPattern("foo")

        assertTrue(("foo" =~ fooPattern).matches())
        assertTrue(fooPattern.matcher("foo").find())
        assertTrue(fooPattern.matcher("foofoo").find())
        assertFalse(fooPattern.matcher("bar").matches())
    }

    void test__findKeyByPattern_null() {

        def cfgLookup = new ConfigLookup()
        def pattern = cfgLookup.buildPattern("foo")

        def emptyMap = cfgLookup.findByKeyPattern(null, pattern)
        assertEquals(0, emptyMap.size())
    }

    void test__findKeyByPattern() {

        def configMap = new HashMap<String, String>()
        configMap.put("foo.bar", "result 1")
        configMap.put("bar", "result 2")
        configMap.put("bar.foo.zoo", "3")

        def cfgLookup = new ConfigLookup()
        def pattern = cfgLookup.buildPattern("foo")

        def configs = cfgLookup.findByKeyPattern(configMap, pattern)
        assertEquals(2, configs.size())
    }
}
