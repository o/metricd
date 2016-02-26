package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Service;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.reader.*;
import org.polimat.metricd.writer.JettyWriter;
import org.polimat.metricd.writer.Slf4jWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InvalidClassException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ConfigurationAwareServiceFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationAwareServiceFactory.class);
    private final Configuration configuration;
    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue = new ArrayBlockingQueue<>(10);
    private final Set<Plugin> plugins = Sets.newHashSet(
            new LoadAverage(),
            new DiskUsage(),
            new Connections(),
            new CpuUsage(),
            new IOStats(),
            new MemoryUsage(),
            new NetworkUsage(),
            new Metricd(),
            new JettyWriter(),
            new Slf4jWriter()
    );

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

        for (Plugin plugin : plugins) {
            try {
                LOGGER.info("Building plugin {}", plugin.getName());
                Set<Plugin> pluginSet = plugin.build(configuration);

                for (Plugin builtPlugin : pluginSet) {
                    if (builtPlugin instanceof AbstractReader) {
                        enabledReaders.add((AbstractReader) builtPlugin);
                        LOGGER.info("{} reader enabled", plugin.getName());
                    } else if (builtPlugin instanceof AbstractWriter) {
                        enabledWriters.add((AbstractWriter) builtPlugin);
                        LOGGER.info("{} writer enabled", plugin.getName());
                    } else {
                        throw new InvalidClassException(plugin.getClass().getName(), "Unexpected plugin type");
                    }
                }


            } catch (Exception e) {
                LOGGER.warn("Plugin {} disabled, cause: {}", plugin.getName(), e.getMessage());
            }
        }

        LOGGER.info("Initialized {} reader and {} writer plugin in {} ms", enabledReaders.size(), enabledWriters.size(), stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }


}
