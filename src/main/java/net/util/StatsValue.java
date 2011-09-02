package net.util;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Tracking the following stats each time a Key is accessed:
 * 1) count
 * 2) average latency
 * 3) last accessed
 * 4) associated patterns
 *
 * A ReentrantLock is used to ensure thread safe updates for _count and _averageLatency.
 *
 * If there is a single associated pattern and it is equal() to the _key, then this is a 1:1
 * property style lookup.
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
public class StatsValue {

    /**
     * Used to protect updates for _count and _averageLatency;
     */
    private final ReentrantLock _lock = new ReentrantLock();

    /**
     * The key for a specific value in the config map
     */
    private final String _key;

    /**
     * The associated pattern(s) for this lookup (Methinks grep/uniq over the codebase would help).
     */
    private final Set<String> _associatedPatterns;

    /** How many times the key has been accessed */
    private long _count;
    /** Average execution time to retrieve in nano seconds */
    private double _averageLatency;
    /** Last time key was accessed in nano seconds */
    private long _lastAccessed;

    public StatsValue(String key) {

        _key = key;
        _associatedPatterns = new HashSet<String>();
    }

    /**
     * Updates with a ReentrantLock:
     * _count
     * _averageLatency
     *
     * Updates with the current thread (is never locked)
     * _lastAccessed
     * _associatedPatterns
     *
     * @param latency The execution time for this key lookup.
     * @param pattern The associated pattern for this lookup.
     */
    public void updateStats(long latency, String pattern) {

        _lock.lock();
        try
        {
            _count++;
            _averageLatency = ((latency - _averageLatency) / _count) + _averageLatency;
        }
        finally
        {
            _lock.unlock();
        }

        // Just concerned with tracking the latest access -- stepping on it is not a big deal.
        _lastAccessed = System.nanoTime();

        if ( pattern != null )
        {
            _associatedPatterns.add(pattern);
        }
    }

    public String getKey() {
        return _key;
    }

    public Set<String> getAssociatedPatterns() {
        return _associatedPatterns;
    }

    public long getCount() {
        return _count;
    }

    public double getAverageLatency() {
        return _averageLatency;
    }

    public long getLastAccessed() {
        return _lastAccessed;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
        {
            return true;
        }

        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        StatsValue that = (StatsValue) o;

        if (Double.compare(that._averageLatency, _averageLatency) != 0)
        {
            return false;
        }

        if (_count != that._count)
        {
            return false;
        }

        if (_lastAccessed != that._lastAccessed)
        {
            return false;
        }

        if (!_associatedPatterns.equals(that._associatedPatterns))
        {
            return false;
        }

        if (!_key.equals(that._key))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result;
        long temp;
        result = _key.hashCode();

        result = 31 * result + _associatedPatterns.hashCode();
        result = 31 * result + (int) (_count ^ (_count >>> 32));
        temp = _averageLatency != +0.0d ? Double.doubleToLongBits(_averageLatency) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (_lastAccessed ^ (_lastAccessed >>> 32));

        return result;
    }

    @Override
    public String toString() {
        return "StatsValue{" +
                "_key='" + _key + '\'' +
                ", _count=" + _count +
                ", _averageLatency=" + _averageLatency +
                ", _lastAccessed=" + _lastAccessed +
                ", _associatedPatterns=" + _associatedPatterns +
                '}';
    }
}
