package org.polimat.metricd.reader;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;
import org.polimat.metricd.util.IOUtils;
import org.polimat.metricd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class IOStats extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOStats.class);

    private static final String FILENAME_SYS_BLOCK_STAT = "/sys/block/%s/stat";

    private static final String FILENAME_PROC_DISK_STATS = "/proc/diskstats";
    private static final Pattern PHYSICAL_DISK_NAME_PATTERN =
            Pattern.compile("(\\w+da\\d?)\\s", Pattern.MULTILINE);
    private static final Long SECTOR_SIZE = 512L;

    private final File diskStatsFile = new File(FILENAME_PROC_DISK_STATS);
    private File blockStatsFile;

    private Long lastReadOps = 0L;
    private Long lastWriteOps = 0L;
    private Long lastReadSectors = 0L;
    private Long lastWriteSectors = 0L;
    private Long lastReadMerged = 0L;
    private Long lastWriteMerged = 0L;

    private Boolean isFirstRun = true;

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        String line;
        try {
            line = getBlockStatsFileContents();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return metrics;
        }

        List<String> stats = Splitter.on(CharMatcher.WHITESPACE)
                .trimResults()
                .omitEmptyStrings()
                .splitToList(line);


        Long currentReadOps = Long.parseLong(stats.get(0));
        Long currentReadMerged = Long.parseLong(stats.get(1));
        Long currentWriteOps = Long.parseLong(stats.get(4));
        Long currentWriteMerged = Long.parseLong(stats.get(5));
        Long currentReadSectors = Long.parseLong(stats.get(2));
        Long currentWriteSectors = Long.parseLong(stats.get(6));

        Long readOpsDiff = (currentReadOps - lastReadOps) / REPORT_PERIOD;
        Long writeOpsDiff = (currentWriteOps - lastWriteOps) / REPORT_PERIOD;
        Long readSectorsDiff = (currentReadSectors - lastReadSectors) / REPORT_PERIOD;
        Long writeSectorsDiff = (currentWriteSectors - lastWriteSectors) / REPORT_PERIOD;
        Long readMergedDiff = (currentReadMerged - lastReadMerged) / REPORT_PERIOD;
        Long writeMergedDiff = (currentWriteMerged - lastWriteMerged) / REPORT_PERIOD;

        Long readBytes = readSectorsDiff * SECTOR_SIZE;
        Long writeBytes = writeSectorsDiff * SECTOR_SIZE;

        lastReadOps = currentReadOps;
        lastWriteOps = currentWriteOps;
        lastReadSectors = currentReadSectors;
        lastWriteSectors = currentWriteSectors;
        lastReadMerged = currentReadMerged;
        lastWriteMerged = currentWriteMerged;

        if (isFirstRun) {
            LOGGER.info("Discarding events for first run");
            isFirstRun = false;
            return metrics;
        }

        metrics.add(new Metric<>(
                "Disk write operations", "metricd/io/ops/write", writeOpsDiff,
                State.OK,
                String.format(
                        "Write octets: %d bytes, Write operations: %d/sec, Write sectors: %d/sec",
                        writeBytes, writeOpsDiff, writeSectorsDiff
                )
        ));

        metrics.add(new Metric<>(
                "Disk read operations", "metricd/io/ops/read", readOpsDiff,
                State.OK,
                String.format(
                        "Read octets: %d bytes, Read operations: %d/sec, Read sectors: %d/sec",
                        readBytes, readOpsDiff, readSectorsDiff
                )

        ));

        metrics.add(new Metric<>("Disk write bytes", "metricd/io/octets/write", writeBytes));
        metrics.add(new Metric<>("Disk read bytes", "metricd/io/octets/read", readBytes));

        metrics.add(new Metric<>("Merged write operations", "metricd/io/merged/write", writeMergedDiff));
        metrics.add(new Metric<>("Merged read operations", "metricd/io/merged/read", readMergedDiff));

        // TODO: add metricd/io/time/write and metricd/io/time/read
        return metrics;
    }

    @Override
    public String getName() {
        return "I/O statistics";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(diskStatsFile);
        extractBlockName();
        IOUtils.checkFile(blockStatsFile);
    }

    private void extractBlockName() throws IOException {
        String line = getDiskStatsFileContents().trim();
        String foundDeviceName = StringUtils.getFirstMatchFromString(PHYSICAL_DISK_NAME_PATTERN, line);

        if (null != foundDeviceName) {
            blockStatsFile = new File(String.format(FILENAME_SYS_BLOCK_STAT, foundDeviceName));
            LOGGER.info("Disk device found : {}", foundDeviceName);
        } else {
            throw new FileNotFoundException("Unable to guess disk device name");
        }
    }

    private String getDiskStatsFileContents() throws IOException {
        return FileUtils.readFileToString(diskStatsFile);
    }

    private String getBlockStatsFileContents() throws IOException {
        return FileUtils.readFileToString(blockStatsFile);
    }

}
