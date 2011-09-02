#jConfigMap (See LICENSE AND NOTICE):#

Is a Java client configuration tool that uses Groovy (for now), to flatten
XML and JSON configuration files into a key-value Map structure. Each key
and value are stored as String values. Value retrieval is based upon applying regular
expressions across the Map key set. The matching values are then returned in a new map.
Or, if it is a simple key value property, then only the matching value is returned.

Currently jConfigMap supports XML and JSON configuration files during startup. A global config
map (see java ConfigMap), can then be used statically for config access.

##Features##
* Supports XML/JSON property style key-value pairs (see 'keyValues')
* Supports XML/JSON object/config structure (see 'structures')
* Supports versioning for certain XML/JSON config structures
* Multiple config strategies available (see JConfigProperties):
  * classpath, url, specified location, command line, environmental filters
* Supports statistic gathering for config access (see ConfigStatistics and StatsValue)
  * count, average latency, associated patterns

####*easy to use*####
* It took approximately 10 minutes to port a 2600 line XML file into the test directory
  and add a handful of unit tests.

* Wrap your XML file with <config><structures> your xml here </structures></config> and put in
  the "test/resources" directory. Add your test class and methods (see PojoConfigExampleTest or
  ConfigEnumStructuredXmlTest)

##Usage (additional examples in test directory)##
####*code sample*####
```java
// See XML configuration files below (ConfigLookup has a number of client utility methods)
ConfigLookup configHelper = new ConfigLookup()

// Simple 1:1 config key-value lookup
Pattern keyOne = Pattern.compile("key.one.string");
assertEquals("first value", configHelper.getByKey(keyOne));

Pattern keyTwo = Pattern.compile("key.two.int");
assertEquals(1, configHelper.getByKey(keyTwo, Integer.class));

Pattern keyThree = Pattern.compile("key.three.double");
assertEquals(2.0, configHelper.getByKey(keyThree, Double.class));

// Produces a List<String> result
Pattern keyFour = Pattern.compile("key.four.list");
assertEquals(4, configHelper.getByKey(keyFour, List.class).size());

// Structured XML configs (something more complex than 1:1)
Pattern stocks = Pattern.compile(".*stocks.*");

// Gather statistics (off by default)
ConfigStatistics.enableStatsCollection();

// 4 results (all stocks)
Map<String, String> stocksMap = configHelper.get(stocks);

// 2 results (values: 8.00, 8.32)
Map<String, String> fooStocks = configHelper.get(stocks, "FOO");

// 2 results (values: 8.00, 4.50)
Map<String, String> lowStocks = configHelper.get(stocks, "low");

// 1 result (values: 8.00)
Map<String, String> lowFooStocks = configHelper.get(stocks, "FOO", "low");
ConfigStatistics.disableStatsCollection();

// Examine the stats
Map<String, StatsValue> stats = ConfigStatistics.getStats();
assertEquals(4, stats.size());
```
####*config sample*####
```
<config>
  <keyValues>
    <property name="key.one.string" value="first value" />
    <property name="key.two.int">1</property>
    <property name="key.three.double" value="2.0" />
    <property name="key.four.list" value="AMD, INTC, WFMI, SCCO" />
  </keyValues>
  <structures>
    <stocks>
        <stock name="FOO">
           <low>8.00</low>
           <high>8.32</high>
        </stock>
        <stock name="BAR">
           <low>4.50</low>
           <high>4.65</high>
        </stock>
    </stocks>
    <cities>
      <Chicago>
        <bars>
          <bar>Sheffields</bar>
          <bar>Map Room</bar>
          <bar>Redmonds</bar>
        </bars>
      </Chicago>
      <Ann Arbor>
        <bars>
          <bar>Grizzly Peak</bar>
        </bars>
      </Ann Arbor>
    </cities>
  </structures>
</config>
```
####*generates*####
  1. stocks.stock.name.foo.low, 8.00
  2. stocks.stock.name.foo.high, 8.32
  3. stocks.stock.name.bar.low, 4.50
  4. stocks.stock.name.bar.high, 4.65
  5. cities.chicago.bars.bar, Sheffields
  6. cities.chicago.bars.bar.1, Map Room
  7. cities.chicago.bars.bar.2, Redmonds
  8. cities.ann arbor.bars.bar, Grizzly Peak

####*notes*####
* Config format (xml, json)
  * Each config file has a 'config' root node and either/both 'keyValues' and 'structures'
  * 'keyValues' nodes return a String, List, or primitive wrapped object
  * XML attribute 'value' stores the corresponding value
  * 'structures' nodes always return a Map<String,String>
  * 'structures' config map results support comparators
  * 'structures' can have ~versioned key-value pairs (see "Bar" example below)
* Config loading options
  1. Default config location is "classpath/config" directory
  2. Remote URL file location
  3. System properties override location "jConfigMap.location"
  4. Specify command line configs with "jConfigMap.entry.name.foo=42" (where "name.foo" is the map key)
  5. Environment specific config file loading. Ex "SomeConfig_dev.xml"
* Config statistics
  * Statistics stored by key (flattened from config)
  * Configuration access count
  * Average latency
  * Associated patterns (paths to this key lookup)

##Future##
1. Clojure implementation and client
2. Scala implementation and client
3. Support encrypted values (not sure its' a good idea yet -- leaning towards not)

##Requirements##
jdk 1.6
groovy 1.8.0
* built by gradle
