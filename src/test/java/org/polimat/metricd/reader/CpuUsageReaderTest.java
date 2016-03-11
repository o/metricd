package org.polimat.metricd.reader;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.mockito.Mockito;
import org.polimat.metricd.Metric;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;

public class CpuUsageReaderTest {

    @Test
    public void testCollect() throws Exception {
        String contents1 = FileUtils.readFileToString(new File("src/test/resources/stat1.txt"));
        String contents2 = FileUtils.readFileToString(new File("src/test/resources/stat2.txt"));

        CpuUsageReader cpuUsageReader = Mockito.spy(new CpuUsageReader());
        doReturn(contents1, contents2).when(cpuUsageReader).getFileContents();
        List<Metric> firstMetrics = cpuUsageReader.collect();
        assertEquals(0, firstMetrics.size());
        List<Metric> aMetricList = cpuUsageReader.collect();
        assertEquals(12, aMetricList.size());
    }


}
