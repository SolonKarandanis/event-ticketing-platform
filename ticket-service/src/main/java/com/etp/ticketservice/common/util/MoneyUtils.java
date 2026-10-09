package com.etp.ticketservice.common.util;

// Single place the x100/÷100 conversion between a human-facing decimal amount (e.g.
// 19.99) and the integer minor units this app stores/sends to Stripe (e.g. 1999L)
// happens -- see issue #23. Every call site that used to hand-roll Math.round(x * 100)
// funnels through here instead.
public final class MoneyUtils {

    private static final int MINOR_UNITS_PER_MAJOR_UNIT = 100;

    private MoneyUtils() {
    }

    public static Long toMinorUnits(Double majorUnitsAmount) {
        if (null == majorUnitsAmount) {
            return null;
        }
        return Math.round(majorUnitsAmount * MINOR_UNITS_PER_MAJOR_UNIT);
    }

    public static Double toMajorUnits(Long minorUnitsAmount) {
        if (null == minorUnitsAmount) {
            return null;
        }
        return minorUnitsAmount / (double) MINOR_UNITS_PER_MAJOR_UNIT;
    }
}
