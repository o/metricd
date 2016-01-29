package org.polimat.metricd.util;

public class MathUtils {

    private static final Long ONE_MEGABYTE_AS_BYTE = 1048576L;

    public static Double getPercent(final Long n, final Long total) {
        return (n * 100d) / total;
    }

    public static Long convertBytesToMb(final Long bytes) {
        return bytes / ONE_MEGABYTE_AS_BYTE;
    }

}
