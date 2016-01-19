package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ReaderManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderManager.class);

    private final Set<AbstractReader> enabledReaders = Sets.newConcurrentHashSet();

    public ReaderManager(Set<AbstractReader> readers) {
        initializeReaders(readers);
    }

    private void initializeReaders(Set<AbstractReader> readers) {
        LOGGER.info("Initializing readers");
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (AbstractReader reader : readers) {
            try {
                LOGGER.info("Starting up {}", reader.getName());
                reader.startUp();

                LOGGER.info("{} enabled", reader.getName());
                addToEnabledReaders(reader);
            } catch (Exception e) {
                LOGGER.warn("{} disabled, {}", reader.getName(), e.getMessage());
            }
        }

        LOGGER.info("Readers initialized in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
    }

    private void addToEnabledReaders(AbstractReader reader) {
        enabledReaders.add(reader);
    }

    public Set<AbstractReader> getEnabledReaders() {
        return enabledReaders;
    }
}
