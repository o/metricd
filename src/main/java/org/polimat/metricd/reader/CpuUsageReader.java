package org.polimat.metricd.reader;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Threshold;
import org.polimat.metricd.util.DerivedMetricUtils;
import org.polimat.metricd.util.IOUtils;
import org.polimat.metricd.util.MathUtils;
import org.polimat.metricd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CpuUsageReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(CpuUsageReader.class);

    private static final String FILENAME_PROC_STAT = "/proc/stat";
    private static final Pattern CPU_JIFFIES_PATTERN =
            Pattern.compile("cpu\\s+(.*)", Pattern.MULTILINE);
    private final File statFile = new File(FILENAME_PROC_STAT);

    private final Integer cpuCores = Runtime.getRuntime().availableProcessors();

    private final DerivedMetricUtils derivedMetricUtils = new DerivedMetricUtils();

    private Boolean isFirstRun = true;

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        String lines;
        try {
            lines = getFileContents();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return metrics;
        }

        String line = StringUtils.getFirstMatchFromString(CPU_JIFFIES_PATTERN, lines);
        if (null == line) {
            LOGGER.error("Unable to find CPU statistics");
            return metrics;
        }

        List<String> stats = Splitter.on(CharMatcher.WHITESPACE)
                .trimResults()
                .omitEmptyStrings()
                .splitToList(line);

        Long currentUser = Long.parseLong(stats.get(0));
        Long currentNice = Long.parseLong(stats.get(1));
        Long currentSystem = Long.parseLong(stats.get(2));
        Long currentIdle = Long.parseLong(stats.get(3));
        Long currentIoWait = Long.parseLong(stats.get(4));
        Long currentIrq = Long.parseLong(stats.get(5));
        Long currentSoftIrq = Long.parseLong(stats.get(6));
        Long currentSteal = Long.parseLong(stats.get(7));
        Long currentGuest = Long.parseLong(stats.get(8));
        Long currentGuestNice = Long.parseLong(stats.get(9));
        Long currentTotalJiffies = 0L;
        for (String jiffy : stats) {
            currentTotalJiffies += Long.parseLong(jiffy);
        }

        Long userDiff = derivedMetricUtils.getDifference("user", currentUser);
        Long niceDiff = derivedMetricUtils.getDifference("nice", currentNice);
        Long systemDiff = derivedMetricUtils.getDifference("system", currentSystem);
        Long idleDiff = derivedMetricUtils.getDifference("idle", currentIdle);
        Long ioWaitDiff = derivedMetricUtils.getDifference("iowait", currentIoWait);
        Long irqDiff = derivedMetricUtils.getDifference("irq", currentIrq);
        Long softIrqDiff = derivedMetricUtils.getDifference("softirq", currentSoftIrq);
        Long stealDiff = derivedMetricUtils.getDifference("steal", currentSteal);
        Long guestDiff = derivedMetricUtils.getDifference("guest", currentGuest);
        Long guestNiceDiff = derivedMetricUtils.getDifference("guestnice", currentGuestNice);
        Long totalJiffiesDiff = derivedMetricUtils.getDifference("total", currentTotalJiffies);

        Double workPercent = MathUtils.getPercent(userDiff + niceDiff + systemDiff, totalJiffiesDiff);
        Double userPercent = MathUtils.getPercent(userDiff, totalJiffiesDiff);
        Double nicePercent = MathUtils.getPercent(niceDiff, totalJiffiesDiff);
        Double systemPercent = MathUtils.getPercent(systemDiff, totalJiffiesDiff);
        Double idlePercent = MathUtils.getPercent(idleDiff, totalJiffiesDiff);
        Double ioWaitPercent = MathUtils.getPercent(ioWaitDiff, totalJiffiesDiff);
        Double irqPercent = MathUtils.getPercent(irqDiff, totalJiffiesDiff);
        Double softIrqPercent = MathUtils.getPercent(softIrqDiff, totalJiffiesDiff);
        Double stealPercent = MathUtils.getPercent(stealDiff, totalJiffiesDiff);
        Double guestPercent = MathUtils.getPercent(guestDiff, totalJiffiesDiff);
        Double guestNicePercent = MathUtils.getPercent(guestNiceDiff, totalJiffiesDiff);

        if (isFirstRun) {
            LOGGER.info("Discarding events for first run");
            isFirstRun = false;
            return metrics;
        }

        metrics.add(new Metric<>(
                "Cpu Usage", "metricd/cpu/usage", workPercent,
                Threshold.getState(workPercent),
                String.format(
                        "User + Nice + System: %f, Idle: %f, IOWait: %f, IRQ: %f, Steal: %f, Guest: %f",
                        workPercent, idlePercent, ioWaitPercent, irqPercent, stealPercent, guestPercent
                )
        ));

        metrics.add(new Metric<>("CPU user", "metricd/cpu/user", userPercent));
        metrics.add(new Metric<>("CPU nice", "metricd/cpu/nice", nicePercent));
        metrics.add(new Metric<>("CPU system", "metricd/cpu/system", systemPercent));
        metrics.add(new Metric<>("CPU idle", "metricd/cpu/idle", idlePercent));
        metrics.add(new Metric<>("CPU IO Wait", "metricd/cpu/iowait", ioWaitPercent));
        metrics.add(new Metric<>("CPU IRQ", "metricd/cpu/irq", irqPercent));
        metrics.add(new Metric<>("CPU soft IRQ", "metricd/cpu/softirq", softIrqPercent));
        metrics.add(new Metric<>("CPU steal", "metricd/cpu/steal", stealPercent));
        metrics.add(new Metric<>("CPU guest", "metricd/cpu/guest", guestPercent));
        metrics.add(new Metric<>("CPU guest nice", "metricd/cpu/guestnice", guestNicePercent));
        metrics.add(new Metric<>("CPU cores", "metricd/cpu/core", cpuCores));

        return metrics;
    }

    @Override
    public String getName() {
        return "CPU Usage";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(statFile);
    }

    protected String getFileContents() throws IOException {
        return FileUtils.readFileToString(statFile);
    }
}
