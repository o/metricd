package org.polimat.metricd.util;

public class MathUtils {

    private static final Long ONE_MEGABYTE_AS_BYTE = 1048576L;

    public static double getPercent(final long n, final long total) {
        return (n * 100f) / total;
    }

    public static long convertBytesToMb(long bytes) {
        return bytes / ONE_MEGABYTE_AS_BYTE;
    }

}
