package org.polimat.metricd;

import com.google.common.collect.Maps;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.polimat.metricd.util.DerivedMetricUtils;
import org.polimat.metricd.util.MathUtils;
import org.polimat.metricd.util.StringUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;


public class UtilsTest {

    @Test
    public void testGetFirstMatchFromString() throws Exception {
        Pattern diskPattern = Pattern.compile("((h|s|xv|v)d\\w\\d?)", Pattern.MULTILINE);
        String contents = FileUtils.readFileToString(new File("src/test/resources/diskstats.txt"));
        assertEquals("sda", StringUtils.getFirstMatchFromString(diskPattern, contents));
        List<String> matches = StringUtils.getAllMatchesFromString(diskPattern, contents);
        assertEquals(2, matches.size());
        assertEquals("sda1", matches.get(1));
    }

    @Test
    public void getDifferenceTest() {
        DerivedMetricUtils util = new DerivedMetricUtils();
        assertEquals(0, util.getDifference("foo", 100));
        assertEquals(10, util.getDifference("foo", 110));
        assertEquals(5, util.getDifference("foo", 115));
        assertEquals(20, util.getDifference("foo", 135));
    }

    @Test
    public void getDifferenceWithRateTest() {
        DerivedMetricUtils util = new DerivedMetricUtils();
        assertEquals(0, util.getDifferenceWithRate("foo", 100));
        assertEquals(10, util.getDifferenceWithRate("foo", 200));
        assertEquals(20, util.getDifferenceWithRate("foo", 400));
        assertEquals(4, util.getDifferenceWithRate("foo", 440));
    }

    @Test
    public void testHumanReadableByteCount() {
        assertEquals("343 bytes", MathUtils.humanReadableByteCount(343));
        assertEquals("238.95 kB", MathUtils.humanReadableByteCount(238947));
        assertEquals("1.41 MB", MathUtils.humanReadableByteCount(1412343));
    }

    @Test
    public void testGetPercent() {
        assertEquals(20, MathUtils.getPercent(20, 100), 0);
        assertEquals(23, MathUtils.getPercent(46, 200), 0);
        assertEquals(2.74, MathUtils.getPercent(100, 3645), 0);
        assertEquals(19.2, MathUtils.getPercent(886, 4612), 0);
    }

}
