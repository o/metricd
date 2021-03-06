package org.polimat.metricd.reader;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Threshold;
import org.polimat.metricd.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoadAverageReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoadAverageReader.class);

    private static final String FILENAME_PROC_LOAD_AVG = "/proc/loadavg";

    private final File loadAvgFile = new File(FILENAME_PROC_LOAD_AVG);

    private final Integer warningLevel = Runtime.getRuntime().availableProcessors();
    private final Integer criticalLevel = warningLevel * 2;

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        String line;
        try {
            line = getFileContents();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return metrics;
        }

        List<String> averages = Splitter.on(CharMatcher.WHITESPACE)
                .trimResults()
                .omitEmptyStrings()
                .splitToList(line);

        Double oneMinuteAverage = Double.valueOf(averages.get(0));
        Double fiveMinuteAverage = Double.valueOf(averages.get(1));
        Double fifteenMinuteAverage = Double.valueOf(averages.get(2));

        metrics.add(new Metric<>(
                "Load average 1 minute", "metricd/load/shortterm", oneMinuteAverage,
                Threshold.getState(oneMinuteAverage, warningLevel, criticalLevel),
                String.format("1 Minute: %.2f, 5 Minute: %.2f, 15 Minute: %.2f", oneMinuteAverage, fiveMinuteAverage, fifteenMinuteAverage)
        ));


        metrics.add(new Metric<>("Load average 5 minute", "metricd/load/midterm", fiveMinuteAverage));
        metrics.add(new Metric<>("Load average 15 minute", "metricd/load/longterm", fifteenMinuteAverage));

        List<String> processCounts = Splitter.on("/")
                .trimResults()
                .omitEmptyStrings()
                .splitToList(averages.get(3));

        Long runningProcesses = Long.valueOf(processCounts.get(0));
        Long totalProcesses = Long.valueOf(processCounts.get(1));

        metrics.add(new Metric<>("Running process", "metricd/processes/running", runningProcesses));
        metrics.add(new Metric<>("Total process", "metricd/processes/total", totalProcesses));

        return metrics;
    }

    @Override
    public String getName() {
        return "Linux Load averages";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(loadAvgFile);
    }

    private String getFileContents() throws IOException {
        return FileUtils.readFileToString(loadAvgFile);
    }

}
