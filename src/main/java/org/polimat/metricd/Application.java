package org.polimat.metricd;

import com.google.common.collect.Sets;
import org.polimat.metricd.reader.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static final Double VERSION = 1.0;

    public static void main(String[] args) {
        LOGGER.info("{} {}, {} {}, {}", System.getProperty("os.name"), System.getProperty("os.arch"), System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"), System.getProperty("java.vm.name"));
        LOGGER.info("metricd {}, getting up and running..", VERSION);

        CollectorService collectorService = new CollectorService(new ReaderManager(
                Sets.newHashSet(
                        new LoadAverage(),
                        new DiskUsage(),
                        new Connections(),
                        new CpuUsage(),
                        new IOStats(),
                        new MemoryUsage(),
                        new NetworkUsage()
                )
        ));

        collectorService.startAsync();
    }
}
