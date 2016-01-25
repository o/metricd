package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ReaderService extends AbstractScheduledService {

    private static final Integer REPORT_PERIOD = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduledService.class);

    private final Set<AbstractReader> readers;

    private final Set<AbstractReader> enabledReaders = Sets.newConcurrentHashSet();

    private final ExecutorService executorService = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true
    );

    private final CompletionService<List<Metric>> executorCompletionService = new ExecutorCompletionService<>(executorService);

    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue;

    public ReaderService(Set<AbstractReader> readers, ArrayBlockingQueue<List<Metric>> arrayBlockingQueue) {
        this.readers = readers;
        this.arrayBlockingQueue = arrayBlockingQueue;
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.info("Initializing readers");
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (AbstractReader reader : readers) {
            try {
                LOGGER.info("Starting up {}", reader.getName());
                reader.startUp();

                LOGGER.info("{} enabled", reader.getName());
                enabledReaders.add(reader);
            } catch (Exception e) {
                LOGGER.warn("{} disabled, cause: {}", reader.getName(), e.getMessage());
            }
        }

        LOGGER.info("Readers initialized in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

    @Override
    protected void runOneIteration() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("Getting metrics from readers");

        for (AbstractReader reader : enabledReaders) {
            executorCompletionService.submit(reader);
        }

        List<Metric> metricList = new ArrayList<>();

        for (int i = 0; i < enabledReaders.size(); i++) {
            try {
                List<Metric> metrics = executorCompletionService.take().get();
                metricList.addAll(metrics);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("An error occured while executing reader: {}", e.getMessage());
            }
        }

        LOGGER.info("Metrics collected in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        arrayBlockingQueue.put(metricList);
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, REPORT_PERIOD, TimeUnit.SECONDS);
    }


}
