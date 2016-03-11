package org.polimat.metricd.reader;

import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Threshold;
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

public class MemoryUsageReader extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemoryUsageReader.class);

    private static final String FILENAME_PROC_MEM_INFO = "/proc/meminfo";
    private static final Pattern TOTAL_MEMORY_PATTERN =
            Pattern.compile("MemTotal:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern FREE_MEMORY_PATTERN =
            Pattern.compile("MemFree:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern BUFFERS_MEMORY_PATTERN =
            Pattern.compile("Buffers:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern CACHED_MEMORY_PATTERN =
            Pattern.compile("Cached:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern TOTAL_SWAP_PATTERN =
            Pattern.compile("SwapTotal:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern FREE_SWAP_PATTERN =
            Pattern.compile("SwapFree:\\s+(\\d+) kB", Pattern.MULTILINE);
    private static final Pattern CACHED_SWAP_PATTERN =
            Pattern.compile("SwapCached:\\s+(\\d+) kB", Pattern.MULTILINE);
    private final File memInfoFile = new File(FILENAME_PROC_MEM_INFO);

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

        Long totalPhysicalMemorySize = Long.parseLong(StringUtils.getFirstMatchFromString(TOTAL_MEMORY_PATTERN, lines));
        Long freePhysicalMemorySize = Long.parseLong(StringUtils.getFirstMatchFromString(FREE_MEMORY_PATTERN, lines));
        Long bufferedPhysicalMemorySize = Long.parseLong(StringUtils.getFirstMatchFromString(BUFFERS_MEMORY_PATTERN, lines));
        Long cachedPhysicalMemorySize = Long.parseLong(StringUtils.getFirstMatchFromString(CACHED_MEMORY_PATTERN, lines));

        Long totalSwapSpaceSize = Long.parseLong(StringUtils.getFirstMatchFromString(TOTAL_SWAP_PATTERN, lines));
        Long freeSwapSpaceSize = Long.parseLong(StringUtils.getFirstMatchFromString(FREE_SWAP_PATTERN, lines));
        Long cachedSwapSpaceSize = Long.parseLong(StringUtils.getFirstMatchFromString(CACHED_SWAP_PATTERN, lines));

        Long freePhysicalMemorySizeWithBuffersAndCached = freePhysicalMemorySize + bufferedPhysicalMemorySize + cachedPhysicalMemorySize;
        Long usedPhysicalMemorySizeWithoutBuffersAndCached = totalPhysicalMemorySize - freePhysicalMemorySizeWithBuffersAndCached;

        Long freeSwapSpaceSizeWithBuffers = freeSwapSpaceSize + cachedSwapSpaceSize;
        Long usedSwapSpaceSizeWithoutBuffers = totalSwapSpaceSize - freeSwapSpaceSizeWithBuffers;

        Double usedMemoryPercentage = MathUtils.getPercent(usedPhysicalMemorySizeWithoutBuffersAndCached, totalPhysicalMemorySize);
        Double usedSwapPercentage = MathUtils.getPercent(usedSwapSpaceSizeWithoutBuffers, totalSwapSpaceSize);

        metrics.add(new Metric<>(
                "Memory usage", "metricd/memory/usage", usedMemoryPercentage,
                Threshold.getState(usedMemoryPercentage),
                String.format("Total: %d kB, Used: %d kB, Free: %d kB, Buffers: %d kB, Cached: %d kB", totalPhysicalMemorySize, usedPhysicalMemorySizeWithoutBuffersAndCached, freePhysicalMemorySizeWithBuffersAndCached, bufferedPhysicalMemorySize, cachedPhysicalMemorySize)
        ));

        metrics.add(new Metric<>(
                "Swap file usage", "metricd/swap/usage", usedSwapPercentage,
                Threshold.getState(usedSwapPercentage),
                String.format("Total: %d kB, Used: %d kB, Free: %d kB, Cached: %d kB", totalSwapSpaceSize, usedSwapSpaceSizeWithoutBuffers, freeSwapSpaceSizeWithBuffers, cachedSwapSpaceSize)
        ));

        metrics.add(new Metric<>("Total memory", "metricd/memory/total", totalPhysicalMemorySize));
        metrics.add(new Metric<>("Free memory", "metricd/memory/free", freePhysicalMemorySizeWithBuffersAndCached));
        metrics.add(new Metric<>("Used memory", "metricd/memory/used", usedPhysicalMemorySizeWithoutBuffersAndCached));
        metrics.add(new Metric<>("Buffered memory", "metricd/memory/buffered", bufferedPhysicalMemorySize));
        metrics.add(new Metric<>("Cached memory", "metricd/memory/cached", cachedPhysicalMemorySize));

        metrics.add(new Metric<>("Total swap", "metricd/swap/total", totalSwapSpaceSize));
        metrics.add(new Metric<>("Free swap", "metricd/swap/free", freeSwapSpaceSizeWithBuffers));
        metrics.add(new Metric<>("Used swap", "metricd/swap/used", usedSwapSpaceSizeWithoutBuffers));
        metrics.add(new Metric<>("Cached swap", "metricd/swap/cached", cachedSwapSpaceSize));

        return metrics;
    }

    @Override
    public String getName() {
        return "Memory usage statistics";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(memInfoFile);
    }

    private String getFileContents() throws IOException {
        return FileUtils.readFileToString(memInfoFile);
    }

}
