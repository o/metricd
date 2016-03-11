package org.polimat.metricd.config.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.io.FileUtils;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.reader.DiskUsageReader;
import org.polimat.metricd.reader.IOStatsReader;
import org.polimat.metricd.util.IOUtils;
import org.polimat.metricd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class DiskUsageReaderNode extends AbstractNode {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiskUsageReaderNode.class);

    private static final String PROC_DISKSTATS = "/proc/diskstats";

    /**
     * Matches hda (IDE), sda (SATA), xvda (Xen) and vda (Virtualized) disk name patterns
     */
    private static final Pattern DISK_PATTERN = Pattern.compile("((h|s|xv|v)d\\w\\d?)", Pattern.MULTILINE);

    @JsonProperty
    private List<String> devices;

    @Override
    protected Set<Plugin> build() {
        Set<Plugin> plugins = new HashSet<>();
        if (null == devices) {
            autoDiscover();
        }

        if (null == devices) {
            return plugins;
        }

        for (String device : devices) {
            plugins.add(new DiskUsageReader(device));
            plugins.add(new IOStatsReader(device));
        }

        return plugins;
    }

    protected void autoDiscover() {
        try {
            LOGGER.info("Autodiscovering disk devices");
            File diskStatsFile = new File(PROC_DISKSTATS);
            IOUtils.checkFile(diskStatsFile);
            devices = listDevicesFromFile(diskStatsFile);
        } catch (IOException e) {
            LOGGER.error("Unable to autodiscover: " + e.getMessage());
        }

    }

    public List<String> listDevicesFromFile(final File file) throws IOException {
        String contents = FileUtils.readFileToString(file);
        return StringUtils.getAllMatchesFromString(DISK_PATTERN, contents);
    }


    public List<String> getDevices() {
        return devices;
    }

    public DiskUsageReaderNode setDevices(List<String> devices) {
        this.devices = devices;
        return this;
    }


}
