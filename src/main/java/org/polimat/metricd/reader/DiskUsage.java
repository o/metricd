package org.polimat.metricd.reader;

import com.google.common.collect.Sets;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.Threshold;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.util.MathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DiskUsage extends AbstractReader {

    private static final String ROOT_FS = "/";

    private final File rootFSFile = new File(ROOT_FS);

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        Long totalSpace = rootFSFile.getTotalSpace();
        Long usableSpace = rootFSFile.getUsableSpace();
        Long usedSpace = totalSpace - usableSpace;

        Double usagePercentage = MathUtils.getPercent(usedSpace, totalSpace);

        metrics.add(new Metric<>(
                "Disk usage", "metricd/disk/usage", usagePercentage,
                Threshold.getState(usagePercentage),
                String.format(
                        "Size: %d MB, Available: %d MB, Used: %d MB",
                        MathUtils.convertBytesToMb(totalSpace),
                        MathUtils.convertBytesToMb(usableSpace),
                        MathUtils.convertBytesToMb(usedSpace)
                )
        ));

        metrics.add(new Metric<>("Used disk space", "metricd/disk/used", usedSpace));
        metrics.add(new Metric<>("Free disk space", "metricd/disk/free", usableSpace));
        metrics.add(new Metric<>("Total disk size", "metricd/disk/total", totalSpace));

        return metrics;
    }

    @Override
    public String getName() {
        return "Disk space statistics";
    }

    @Override
    public Set<Plugin> build(Configuration configuration) throws Exception {
        if (!rootFSFile.canRead()) {
            throw new SecurityException("Filesystem is not readable");
        }
        return Sets.newHashSet(this);
    }

}
