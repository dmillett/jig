jConfigMap (See LICENSE AND NOTICE):
====================================

Is a Java client configuration tool that uses Groovy (for now), to flatten
XML and JSON configuration files into a key-value Map structure. Each key
and value are stored as String values. Value retrieval is based upon applying regular
expressions across the Map key set. The matching values are then returned in a new map.
Or, if it is a simple key value property, then only the matching value is returned.

Currently jConfigMap supports XML configuration files during startup. A global config
map (see java ConfigMap), can then be used statically for config access.

Usage:
------
* Each config file has a 'config' root node and either/both 'keyValueProperties' and 'xmlStructure'
* 'xmlStructure' nodes always return a Map<String,String>
* 'keyValueProperties' nodes return a String, List, or primitive wrapped object
* 'value' attribute key stores the corresponding value in the config map
* 'xmlStructure' config map results support comparators

* Configuration File(s) Config Map entry order (reverse priority - see JConfigProperties):
    1) Default config location is "classpath/config" directory
    2) Remote URL file location
    3) System properties override location "jConfigMap.location"
    4) Specify command line configs with "jConfigMap.entry.name.foo=42" (where "name.foo" is the map key)

Examples (additional examples in test directory):
-------------------------------------------------

Usage:
-----
```java
ConfigLookup configHelper = new ConfigLookup()
Pattern stocks = Pattern.compile(".*stocks.*");

configHelper.get(stocks) --> 4 results
configHelper.get(stocks, "FOO") --> 2 results
configHelper.get(stocks, "low") --> 2 results
configHelper.get(stocks, "FOO", "low") --> 1 result

Pattern bars = Pattern.compile(".*bars.*);
configHelper.get(bars) --> 4 results
configHelper.get(bars, "Chicago") --> 3 results
```

Configuration:
-------------
```
<config>
  <xmlStructure>
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
  </xmlStructure>
</config>

generates:
stocks.stock.foo.low, 8.00
stocks.stock.foo.high, 8.32
stocks.stock.bar.low, 4.50
stocks.stock.bar.high, 4.65

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
