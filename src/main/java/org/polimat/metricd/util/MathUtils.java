package org.polimat.metricd.util;

public class MathUtils {

    private static final Long ONE_MEGABYTE_AS_BYTE = 1048576L;

    public static Double getPercent(final long n, final long total) {
        if (0 == n) {
            return 0.0;
        }
        return (n * 100d) / total;
    }

    public static Long convertBytesToMb(final long bytes) {
        return bytes / ONE_MEGABYTE_AS_BYTE;
    }

}
