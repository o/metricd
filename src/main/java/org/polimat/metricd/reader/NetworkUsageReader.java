package org.polimat.metricd.reader;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Threshold;
import org.polimat.metricd.util.IOUtils;
import org.polimat.metricd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class NetworkUsageReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkUsageReader.class);

    private static final Pattern ETH0_STATS_PATTERN = Pattern.compile("eth0:\\s+(.*)", Pattern.MULTILINE);

    private static final String FILENAME_PROC_NET_DEV = "/proc/net/dev";

    private final File devFile = new File(FILENAME_PROC_NET_DEV);

    private Long lastRxBytes = 0L;
    private Long lastTxBytes = 0L;
    private Long lastRxErrors = 0L;
    private Long lastTxErrors = 0L;
    private Long lastRxPackets = 0L;
    private Long lastTxPackets = 0L;

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

        String line = StringUtils.getFirstMatchFromString(ETH0_STATS_PATTERN, lines);
        if (null == line) {
            LOGGER.error("Unable to find interface statistics");
            return metrics;
        }

        List<String> stats = Splitter.on(CharMatcher.WHITESPACE)
                .trimResults()
                .omitEmptyStrings()
                .splitToList(line);

        Long currentRxBytes = Long.parseLong(stats.get(0));
        Long currentRxPackets = Long.parseLong(stats.get(1));
        Long currentRxErrors = Long.parseLong(stats.get(2));
        Long currentTxBytes = Long.parseLong(stats.get(8));
        Long currentTxPackets = Long.parseLong(stats.get(9));
        Long currentTxErrors = Long.parseLong(stats.get(10));

        Long rxBytesDiff = (currentRxBytes - lastRxBytes) / REPORT_PERIOD;
        Long rxPacketsDiff = (currentRxPackets - lastRxPackets) / REPORT_PERIOD;
        Long rxErrorsDiff = (currentRxErrors - lastRxErrors) / REPORT_PERIOD;
        Long txBytesDiff = (currentTxBytes - lastTxBytes) / REPORT_PERIOD;
        Long txPacketsDiff = (currentTxPackets - lastTxPackets) / REPORT_PERIOD;
        Long txErrorsDiff = (currentTxErrors - lastTxErrors) / REPORT_PERIOD;

        lastRxBytes = currentRxBytes;
        lastRxErrors = currentRxErrors;
        lastRxPackets = currentRxPackets;

        lastTxBytes = currentTxBytes;
        lastTxErrors = currentTxErrors;
        lastTxPackets = currentTxPackets;

        Double rxBytesDiffMb = (double) rxBytesDiff / 1024L / 1024L;
        Double txBytesDiffMb = (double) txBytesDiff / 1024L / 1024L;

        if (isFirstRun) {
            LOGGER.info("Discarding events for first run");
            isFirstRun = false;
            return metrics;
        }

        metrics.add(new Metric<>(
                "Received bytes", "metricd/network/octets/rx", rxBytesDiff,
                Threshold.getState(rxErrorsDiff, 1, 2),
                String.format("Received: %d bytes, %f MB", rxBytesDiff, rxBytesDiffMb)

        ));

        metrics.add(new Metric<>(
                "Transferred bytes", "metricd/network/octets/tx", txBytesDiff,
                Threshold.getState(txErrorsDiff, 1, 2),
                String.format("Received: %d bytes, %f MB", txBytesDiff, txBytesDiffMb)
        ));

        metrics.add(new Metric<>("Receive errors", "metricd/network/errors/rx", rxErrorsDiff));
        metrics.add(new Metric<>("Transfer errors", "metricd/network/errors/tx", txErrorsDiff));

        metrics.add(new Metric<>("Received packets", "metricd/network/packets/rx", rxPacketsDiff));
        metrics.add(new Metric<>("Transferred packets", "metricd/network/packets/tx", txPacketsDiff));

        return metrics;
    }

    @Override
    public String getName() {
        return "Network statistics";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(devFile);
    }

    private String getFileContents() throws IOException {
        return FileUtils.readFileToString(devFile);
    }

}
