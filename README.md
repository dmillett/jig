jConfigMap (See LICENSE AND NOTICE):
====================================

Is a Java client configuration tool that uses Groovy (for now), to flatten
XML and JSON configuration files into a key-value Map structure. Each key
and value are stored as String values. Value retrieval is based upon applying regular
expressions across the Map key set. The matching values are then returned in a new map.
Or, if it is a simple key value property, then only the matching value is returned.

Currently jConfigMap supports XML and JSON configuration files during startup. A global config
map (see java ConfigMap), can then be used statically for config access.

* A Test implementation  with 2600 line XML file was ported to

Usage:
------
* Each config file has a 'config' root node and either/both 'keyValues' and 'structures'
* 'structures' nodes always return a Map<String,String>
* 'keyValues' nodes return a String, List, or primitive wrapped object
* 'value' attribute key stores the corresponding value in the config map
* 'structures' config map results support comparators
* Configuration File(s) Config Map entry order (reverse priority - see JConfigProperties):
    1) Default config location is "classpath/config" directory
    2) Remote URL file location
    3) System properties override location "jConfigMap.location"
    4) Specify command line configs with "jConfigMap.entry.name.foo=42" (where "name.foo" is the map key)
    5) Environment specific config file loading. Ex "SomeConfig_dev.xml"

Easy to use:
------------
* It took approximately 10 minutes to port a 2600 line XML file into the test directory
  and add a handful of unit tests.

Examples (additional examples in test directory):
-------------------------------------------------

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

// 4 results (all stocks)
Map<String, String> stocksMap = configHelper.get(stocks);

// 2 results (values: 8.00, 8.32)
Map<String, String> fooStocks = configHelper.get(stocks, "FOO");

// 2 results (values: 8.00, 4.50)
Map<String, String> lowStocks = configHelper.get(stocks, "low");

// 1 result (values: 8.00)
Map<String, String> lowFooStocks = configHelper.get(stocks, "FOO", "low");
```

Configuration:
-------------
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

generates:
stocks.stock.name.foo.low, 8.00
stocks.stock.name.foo.high, 8.32
stocks.stock.name.bar.low, 4.50
stocks.stock.name.bar.high, 4.65

// Note the version for Chicago Bars
cities.chicago.bars.bar, Sheffields
cities.chicago.bars.bar.1, Map Room
cities.chicago.bars.bar.2, Redmonds
cities.ann arbor.bars.bar, Grizzly Peak
```

Requirements:
-------------
jdk 1.6
groovy 1.8.0
* built by gradle
