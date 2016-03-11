package org.polimat.metricd.reader;

import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Threshold;
import org.polimat.metricd.util.MathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DiskUsageReader extends AbstractReader {

    private final String device;

    private final File deviceFile;

    public DiskUsageReader(String device) {
        this.device = device;
        this.deviceFile = getLinuxDeviceFile();
    }

    @Override
    public List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        Long totalSpace = deviceFile.getTotalSpace();
        Long usableSpace = deviceFile.getUsableSpace();
        Long usedSpace = totalSpace - usableSpace;

        Double usagePercentage = MathUtils.getPercent(usedSpace, totalSpace);

        metrics.add(new Metric<>(
                String.format("Disk usage %s", device), String.format("metricd/disk/%s/usage", device), usagePercentage,
                Threshold.getState(usagePercentage),
                String.format(
                        "Size: %s, Available: %s, Used: %s",
                        FileUtils.byteCountToDisplaySize(totalSpace),
                        FileUtils.byteCountToDisplaySize(usableSpace),
                        FileUtils.byteCountToDisplaySize(usedSpace)
                )
        ));

        metrics.add(new Metric<>(String.format("Used disk space %s", device), String.format("metricd/disk/%s/used", device), usedSpace));
        metrics.add(new Metric<>(String.format("Free disk space %s", device), String.format("metricd/disk/%s/free", device), usableSpace));
        metrics.add(new Metric<>(String.format("Total disk size %s", device), String.format("metricd/disk/%s/total", device), totalSpace));

        return metrics;
    }

    @Override
    public String getName() {
        return String.format("Disk space statistics [%s]", device);
    }

    @Override
    public void startUp() throws Exception {
        if (!deviceFile.exists()) {
            throw new SecurityException(String.format("Filesystem is not exists [%s]", device));
        }
    }

    protected File getLinuxDeviceFile() {
        return new File("/dev", device);
    }

}
