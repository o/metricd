package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Service;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.reader.*;
import org.polimat.metricd.writer.Jetty;
import org.polimat.metricd.writer.Console;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConfigurationAwareServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationAwareServiceFactory.class);
    private final Configuration configuration;
    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue = new ArrayBlockingQueue<>(10);

    private final Set<AbstractReader> enabledReaders = new HashSet<>();

    private final Set<AbstractWriter> enabledWriters = new HashSet<>();

    public ConfigurationAwareServiceFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    public Set<Service> getServices() {
        return Sets.newHashSet(new ReaderService(enabledReaders, arrayBlockingQueue), new WriterService(enabledWriters, arrayBlockingQueue));
    }

    public void initializePlugins() {
        LOGGER.info("Initializing plugins");
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (Plugin plugin : configuration.getPlugins()) {
            try {
                LOGGER.info("Starting plugin {}", plugin.getName());
                plugin.startUp();

                if (plugin instanceof AbstractReader) {
                    enabledReaders.add((AbstractReader) plugin);
                    LOGGER.info("{} reader enabled", plugin.getName());
                }

                if (plugin instanceof AbstractWriter) {
                    enabledWriters.add((AbstractWriter) plugin);
                    LOGGER.info("{} writer enabled", plugin.getName());
                }

            } catch (Exception e) {
                LOGGER.warn("Plugin {} disabled, cause: {}", plugin.getName(), e.getMessage());
            }
        }

        LOGGER.info("Initialized {} reader and {} writer plugin in {} ms", enabledReaders.size(), enabledWriters.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }


}
