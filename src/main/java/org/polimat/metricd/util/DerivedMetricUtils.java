package org.polimat.metricd.util;


import com.google.common.collect.Maps;
import com.google.common.math.LongMath;

import java.math.RoundingMode;
import java.util.Map;

/**
 * Check-than-act invocation is not thread safe, only useful for thread-local
 */
public class DerivedMetricUtils {

    private static final Integer REPORT_PERIOD = 10;

    private Map<String, Long> map = Maps.newConcurrentMap();

    public long getDifference(String key, long value) {
        if (map.containsKey(key)) {
            Long difference = value - map.get(key);
            map.put(key, value);
            return difference;
        }
        map.put(key, value);
        return 0;
    }

    public long getDifferenceWithRate(String key, long value) {
        return LongMath.divide(getDifference(key, value), REPORT_PERIOD, RoundingMode.HALF_EVEN);
    }

}
