package net.config

/**
 * Created by IntelliJ IDEA.
 * User: dave
 * Date: 5/25/11
 * Time: 1:30 PM
 * To change this template use File | Settings | File Templates.
 */
class JsonFlattenerTest
    extends GroovyTestCase {

    // todo: fix hardcoded files ... investigate url options
    void test__flattenJsonFile() {

        def fileUrl = "file:///home/dave/dev/jConfigMap/src/test/resources/config/ExampleConfigThree.json"
        def jsonFlattener = new JsonFlattener()
        def exampleThreeKeyValues = jsonFlattener.flattenJsonFile(fileUrl)

        assertNotNull(exampleThreeKeyValues)
        assertEquals(8, exampleThreeKeyValues.size())
    }
}
