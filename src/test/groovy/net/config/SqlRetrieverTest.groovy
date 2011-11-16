package net.config

import groovy.sql.Sql

/**
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
class SqlRetrieverTest
    extends GroovyTestCase {

    def static sql

    @Override
    protected void setUp() {

        sql = Sql.newInstance("jdbc:h2:mem:", "test", "", "org.h2.Driver")
        sql.execute("create table CONFIG (id int primary key, key varchar(50), value varchar(50))")
        sql.execute("insert into CONFIG values (1, 'db.one', '1'), (2, 'db.two', 'two'), (3, 'db.three', 'false')")

        def insertedRows = sql.rows("SELECT * FROM CONFIG")
        assertEquals(3, insertedRows.size())
    }

    void test__loadFromDatabase() {

        //def dbParams = buildInMemoryDbParams()

        def sqlRetriever = new SqlRetriever()
        def result = sqlRetriever.loadFromDatabase("config", sql)

        // 3 config key:values for 1 table
        assertEquals(1, result.size())
        assertEquals(3, result.entrySet().iterator().next().value.size())
    }

    private def Map<String, String> buildInMemoryDbParams() {

        def dbParams = new HashMap<String, String>()
        dbParams.put("dbconfigtable.name.bar.tablename", "config")
        dbParams.put("dbconfigtable.name.bar.username", "test")
        dbParams.put("dbconfigtable.name.bar.userpassword", "")
        dbParams.put("dbconfigtable.name.bar.url", "jdbc:h2:mem:")
        dbParams.put("dbconfigtable.name.bar.driver", "org.h2.Driver")

        return dbParams
    }
}
