package net.config.example.one;

import junit.framework.TestCase;
import net.config.example.one.ConfigEnumExample;

import java.util.List;


/**
 * A enum based config usage example.
 * todo: narrowing vie 'T' in the enum
 */
public class ConfigEnumKeyValueExampleTest
    extends TestCase {

    @Override
    protected void setUp()
        throws Exception {

        System.getProperties().put("jConfigMap.location", "/home/dave/dev/jConfigMap/src/test/resources/config");
    }

    public void test__ONE() {

        String test = ConfigEnumExample.ONE.get(String.class);
        assertEquals("first value", test);
    }

    public void test__TWO() {

        int test = Integer.parseInt(ConfigEnumExample.TWO.get(String.class));
        assertEquals(1, test);

        assertEquals((Integer)1, ConfigEnumExample.TWO.get(Integer.class));
    }

    public void test__THREE() {

        assertEquals((Double)2.0, ConfigEnumExample.THREE.get((Double.class)));
    }

    public void test__FOUR() {
        assertTrue(ConfigEnumExample.FOUR.get(Boolean.class));
    }

    public void test__FIVE() {

        String testList = ConfigEnumExample.FIVE.get(String.class);
        assertEquals("AMD, INTC, WFMI, SCCO", testList);

        List<String> testValues = ConfigEnumExample.FIVE.get(List.class);
        assertEquals(4, testValues.size());
    }
}
