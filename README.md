# jig (See LICENSE AND NOTICE):

Is a Java client configuration tool that uses Groovy (for now), to separate client
configuration access from underlying config storage implementations. For example, it flattens
XML and JSON configuration files into a key-value Map structure. Each key and value are stored 
as String values. Value retrieval is based upon applying regular expressions across the Map 
key set. The matching values are then returned in a new map. Or, if it is a simple key value 
property, then only the matching value is returned.

Currently jConfigMap supports XML and JSON configuration files during startup. Database
connections maybe specified in XML/JSON files and retrieved in key-value form. A global config
map (see java ConfigMap), can then be used statically for config access.

## Features
* Supports XML/JSON property style key-value pairs (see 'keyValues')
* Supports XML/JSON object/config structure (see 'structures')
* Supports config retrieval from a database
* Supports versioning for certain XML/JSON config structures
* Multiple config strategies available (see JConfigProperties):
  + classpath, url, specified location, command line, environmental filters
* Supports statistic gathering for config access (see ConfigStatistics and StatsValue)
  + count, average latency, associated patterns
* JMX MBean 

####*installation*
```xml
<dependency>
  <groupId>com.github.dmillett</groupId>
  <artifactId>jig</artifactId>
  <version>0.95</version>
</dependency>
```

See (https://github.com/dmillett/jConfigMap/wiki)
####*easy to use*
* It took approximately 10 minutes to port a 2600 line XML file into the test directory
  and add a handful of unit tests. See (https://github.com/dmillett/jConfigMap/wiki)

##Usage (additional examples in test directory)

###pojo examples
```java
// Retrieve an exact value
ConfigLookup lookup = new ConfigLookup();
int value1 = lookup.getByKey("key.two.int", Integer.class);

// Retrieve a group where all key-value pairs where the key matches this pattern
ConfigLookup configHelper = new ConfigLookup();
Pattern stocks = PatternHelper.buildPattern("stocks");

// 4 results (all stocks #1 - 4)
Map<String, String> stocksMap = configHelper.get(stocks);

// 2 results (see #1, 2 -> values: 8.00, 8.32)
Map<String, String> fooStocks = configHelper.get(stocks, "FOO");

// 2 results (see #1, 3 -> values: 8.00, 4.50)
Map<String, String> lowStocks = configHelper.get(stocks, "low");

// 1 result (see #1 -> values: 8.00)
Map<String, String> lowFooStocks = configHelper.get(stocks, "FOO", "low");
```

###static access with enum (see test example package)
```java

// A sample enum
public enum ConfigEnumExample {

    // key:value pairs
    ONE("key.one.string", String.class),
    TWO("key.two.int", Integer.class),
    THREE("key.three.double", Double.class),
    FOUR("key.four.boolean", Boolean.class),
    FIVE("key.five.list", List.class),
} 

ConfigEnumExample.FOUR.get(Boolean.class));

// As a String or int
int test1 = Integer.parseInt(ConfigEnumExample.TWO.get(String.class));
int test2 = ConfigEnumExample.TWO.get(Integer.class);
assertTrue(test1 == test2);

// Values as a String or List<String>
String testList = ConfigEnumExample.FIVE.get(String.class);
List<String> testValues = ConfigEnumExample.FIVE.get(List.class);
```

###structured config code sample with lists/sorts and statistics
```java
ConfigLookup configHelper = new ConfigLookup()
Pattern stocks = PatternHelper.buildPattern("bars");

// Gather statistics (off by default)
configHelper.getConfigStatistics().enableStatsCollection();

ConfigLookup configHelper = new ConfigLookup();
Pattern bars = PatternHelper.buildPattern("bars");

// All bar key-values (#1 - 4)
Map<String, String> allBars = configHelper.get(bars);

// Sorted bar key-values(#1-4)
Map<String, String> sortedBars = configHelper.getSortedResults(someComparator, bars);

// Chicago bars (#1, 2, 3) note the versions ("", "1", "2")
Map<String, String> chicagoBars = configHelper.get(bars, "chicago");

configHelper.getConfigStatistics().disableStatsCollection();

// Examine the stats
Map<String, StatsValue> stats = ConfigStatistics.getStats();
assertEquals(4, stats.size());

StatsValue value = stats.get("structures.cities.chicago.bars.bar");
assertTrue(value.getAverageLatency() > 0.0);
assertTrue(value.getLastAccessed() > 0);
assertEquals(2, value.getCount());
assertEquals(2, value.getAssociatedPatterns().size());
```

#####*simple key-value config sample in XML*
```xml
<config>
  <keyValues>
    <property name="key.one.string" value="first value" />
    <property name="key.two.int">1</property>
    <property name="key.three.double" value="2.0" />
    <property name="key.four.list" value="AMD, INTC, WFMI, SCCO" />
  </keyValues>
</config>
```
#####*generates Map.Entry*
```
  1. "key.one.string, "first value"
  2. "key.two.int", "1"
  3. "key.three.double", "2.0"
  4. "key.four.list", "AMD, INTC, WFMI, SCCO"
```

#####*structured config sample*
```xml
<config>
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
  </structures>
</config>
```
#####*generates*
```
  1. "structures.stocks.stock.name.foo.low", "8.00"
  2. "structures.stocks.stock.name.foo.high", "8.32"
  3. "structures.stocks.stock.name.bar.low", "4.50"
  4. "structures.stocks.stock.name.bar.high", "4.65"
```

#####*structured config sample with list(s)*
```xml
<config>
  <structures>
    <cities>
      <Chicago>
        <bars>
          <bar>Sheffields</bar>
          <bar>Map Room</bar>
          <bar>Matilda</bar>
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
#####*generates*
```
  1. "structures.cities.chicago.bars.bar", "Sheffields"
  2. "structures.cities.chicago.bars.bar.1", "Map Room"
  3. "structures.cities.chicago.bars.bar.2", "Matilda"
  4. "structures.cities.ann arbor.bars.bar", "Grizzly Peak"
```

###notes
* Config loading options
  1. Default config location is "classpath/config" directory
  2. Remote URL file location
  3. System properties override location "jConfigMap.location"
  4. Specify command line configs with "jConfigMap.entry.name.foo=42" (where "name.foo" is the map key)
  5. Environment specific config file loading. Ex "SomeConfig_dev.xml"
* Config format (xml, json)
  * Each config file has a 'config' root node and either/both 'keyValues' and 'structures'
  * 'keyValues' nodes return a String, List, or primitive wrapped object
  * XML attribute 'value' stores the corresponding value
  * 'structures' nodes always return a Map<String,String>
  * 'structures' config map results support comparators
  * 'structures' can have ~versioned key-value pairs (see "Bar" example below)
* Config statistics
  * Statistics stored by key (flattened from config)
  * Configuration access count
  * Average latency
  * Associated patterns (paths to this key lookup)

##Future
See (https://github.com/dmillett/jig/issues)
