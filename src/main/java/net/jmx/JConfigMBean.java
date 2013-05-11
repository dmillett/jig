package net.jmx;

import net.util.StatsValue;

import java.util.Date;
import java.util.Map;

/**
 * todo: an impl
 * This could be exported via Spring when including reference to any ConfigLookup bean.
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
public interface JConfigMBean {

    /**
     * Get a copy of the current config map. This might be large so use with caution
     * if printing it to a ui.
     */
    public Map<String, Map<String, String>> getCurrentConfig();

    /**
     * Reload the configuration from all sources.
     */
    public void reloadConfig();

    /**
     * Revert the current config version to the previously deployed version.
     */
    public void revertConfig();

    /**
     * The last update to the config map (via load)
     * @return
     */
    public Date getLastUpdate();

    /**
     * Compare and log all differences between current and previous config versions.
     */
    public void compareAndLogConfigVersionDifferences();

    /**
     * Dump the current config key:value entries
     */
    public void dumpCurrentConfig();

    /**
     * Dump the previous version key:value entries
     */
    public void dumpAllConfig();

    // *********** Config statistics

    /** Is statistics collection currently enabled? */
    public void areStatisticsEnabled();
    /** Enable config statistics collection */
    public void enableStatistics();
    /** Disable statistics collection */
    public void disableStatistics();
    /** Dump all statistics to logs. */
    public void dumpStatistics();
    /** Clear all statistics. */
    public void clearStatistics();
    /** Return a copy of current statistics */
    public Map<String, StatsValue> getStatistics();
    /** Get the statistics for a specific config key */
    public StatsValue getStatisticsFor(String key);
}
