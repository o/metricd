package org.polimat.metricd.reader;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;
import org.polimat.metricd.util.DerivedMetricUtils;
import org.polimat.metricd.util.IOUtils;
import org.polimat.metricd.util.MathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IOStatsReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(IOStatsReader.class);

    private static final String FILENAME_SYS_BLOCK_STAT = "/sys/block/%s/stat";

    private final DerivedMetricUtils derivedMetricUtils = new DerivedMetricUtils();

    private final String device;

    private final File blockStatFile;

    private static final Long SECTOR_SIZE = 512L;

    private Boolean isFirstRun = true;

    public IOStatsReader(String device) {
        this.device = device;
        this.blockStatFile = getBlockStatFileFromName();
    }

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        String line;
        try {
            line = getBlockStatFileContents();
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

        Long readOpsDiff = derivedMetricUtils.getDifferenceWithRate("readops", currentReadOps);
        Long writeOpsDiff = derivedMetricUtils.getDifferenceWithRate("writeops", currentWriteOps);
        Long readSectorsDiff = derivedMetricUtils.getDifferenceWithRate("readsec", currentReadSectors);
        Long writeSectorsDiff = derivedMetricUtils.getDifferenceWithRate("writesec", currentWriteSectors);
        Long readMergedDiff = derivedMetricUtils.getDifferenceWithRate("readmerge", currentReadMerged);
        Long writeMergedDiff = derivedMetricUtils.getDifferenceWithRate("writemerge", currentWriteMerged);

        Long readBytes = readSectorsDiff * SECTOR_SIZE;
        Long writeBytes = writeSectorsDiff * SECTOR_SIZE;

        if (isFirstRun) {
            LOGGER.info("Discarding events for first run");
            isFirstRun = false;
            return metrics;
        }

        metrics.add(new Metric<>(
                String.format("Disk write operations %s", device), String.format("metricd/io/%s/ops/write", device), writeOpsDiff,
                State.OK,
                String.format(
                        "Write octets: %s, Write operations: %d/sec, Write sectors: %d/sec",
                        MathUtils.humanReadableByteCount(writeBytes), writeOpsDiff, writeSectorsDiff
                )
        ));

        metrics.add(new Metric<>(
                String.format("Disk read operations %s", device), String.format("metricd/io/%s/ops/read", device), readOpsDiff,
                State.OK,
                String.format(
                        "Read octets: %s, Read operations: %d/sec, Read sectors: %d/sec",
                        MathUtils.humanReadableByteCount(readBytes), readOpsDiff, readSectorsDiff
                )

        ));

        metrics.add(new Metric<>(String.format("Disk write bytes %s", device), String.format("metricd/io/%s/octets/write", device), writeBytes));
        metrics.add(new Metric<>(String.format("Disk read bytes %s", device), String.format("metricd/io/%s/octets/read", device), readBytes));

        metrics.add(new Metric<>(String.format("Merged write operations %s", device), String.format("metricd/io/%s/merged/write", device), writeMergedDiff));
        metrics.add(new Metric<>(String.format("Merged read operations %s", device), String.format("metricd/io/%s/merged/read", device), readMergedDiff));

        // TODO: add metricd/io/time/write and metricd/io/time/read
        return metrics;
    }

    @Override
    public String getName() {
        return String.format("I/O statistics [%s]", device);
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(blockStatFile);
    }

    private String getBlockStatFileContents() throws IOException {
        return FileUtils.readFileToString(blockStatFile);
    }

    protected File getBlockStatFileFromName() {
        return new File(String.format(FILENAME_SYS_BLOCK_STAT, device));
    }


}
