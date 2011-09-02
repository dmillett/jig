package net.util;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This gathers statistics for ConfigMap use. It will track things like:
 * 1) count
 * 2) performance
 * 3) last accessed
 *
 * Statistic collection is optional, since collecting data will adversely
 * affect performance.
 *
 * Using this for now
 * Approach 1: use a HashMap with thread locking on specific StatsValue when updating.
 *
 * Maybe later
 * Approach 2: use a ConcurrentHashMap with immutable values, but more garbage collection
 *
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
 * limitations under the License.
 */
public class ConfigStatistics {

    private static final Logger LOG = Logger.getLogger(ConfigStatistics.class);
    private static final ReentrantLock LOCK = new ReentrantLock();

    /** Where the values are thread aware (volatile) mutable objects */
    private static final Map<String, StatsValue> STATS = new HashMap<String, StatsValue>();
    /** To capture or not to capture */
    private static boolean _statsCaptureEnabled;

    /**
     * Add or update statistics for a Key. Note that the value 'StatsValue'
     * object is mutable and holds the updating state. There is a Lock
     * placed whenever a key is inserted into STATS, otherwise, it relies
     * on StatsValue to maintain thread safe consistent data.
     *
     * @param key The key in the map returned by the keyset pattern query
     * @param latency How long to retrieve the result
     * @param pattern The pattern the resulted in this 'key' lookup
     */
    public static void addKeyLookup(String key, long latency, String pattern) {

        StatsValue storedValue = STATS.get(key);

        if ( STATS.get(key) != null )
        {
            storedValue.updateStats(latency, pattern);
        }
        else
        {
            LOCK.lock();
            try
            {
                StatsValue value = STATS.get(key);

                if ( value == null )
                {
                    value = new StatsValue(key);
                    value.updateStats(latency, pattern);
                    STATS.put(key, value);
                }
                else
                {
                    STATS.get(key).updateStats(latency, pattern);
                }
            }
            finally
            {
                LOCK.unlock();
            }
        }
    }

    public static Map<String, StatsValue> getStats() {
        return new HashMap<String, StatsValue>(STATS);
    }

    public static boolean isEnabled() {
        return _statsCaptureEnabled;
    }

    public static void dumpOutput() {

        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, StatsValue> entry : STATS.entrySet())
        {
            sb.append(entry.getKey()).append(":").append(entry.getValue()).append("|");
        }

        LOG.info(sb.toString());
    }

    public static synchronized void clearStatistics() {
        STATS.clear();
    }

    public static synchronized void disableStatsCollection() {
        _statsCaptureEnabled = false;
    }

    public static synchronized void enableStatsCollection() {
        _statsCaptureEnabled = true;
    }
}
