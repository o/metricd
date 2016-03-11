package org.polimat.metricd.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MathUtils {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private static final Integer THOUSAND = 1000;

    private static final Integer PERCENT_PRECISION = 3;

    public static double getPercent(final long n, final long total) {
        if (0 == n) {
            return 0;
        }

        return BigDecimal.valueOf(n).multiply(HUNDRED).divide(BigDecimal.valueOf(total), new MathContext(PERCENT_PRECISION, RoundingMode.HALF_EVEN)).doubleValue();
    }

    public static String humanReadableByteCount(final long bytes) {
        if (bytes < THOUSAND) return bytes + " bytes";
        Integer exp = (int) (Math.log(bytes) / Math.log(THOUSAND));
        Character pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(THOUSAND, exp), pre);
    }

}
