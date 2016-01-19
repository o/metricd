package org.polimat.metricd;

public class Threshold {

    private static final int DEFAULT_WARNING_LEVEL = 70;

    private static final int DEFAULT_CRITICAL_LEVEL = 90;

    public static State getState(final double value, final int warningLevel, final int criticalLevel) {
        if (warningLevel == 0 || criticalLevel == 0) {
            return State.OK;
        }

        if (value < warningLevel) {
            return State.OK;
        } else if (value >= warningLevel && value < criticalLevel) {
            return State.WARNING;
        } else {
            return State.CRITICAL;
        }
    }

    public static State getState(final double value) {
        return getState(value, DEFAULT_WARNING_LEVEL, DEFAULT_CRITICAL_LEVEL);
    }


}
