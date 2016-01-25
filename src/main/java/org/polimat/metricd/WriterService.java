package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class WriterService extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduledService.class);

    private final Set<AbstractWriter> writers;

    private final Set<AbstractWriter> enabledWriters = Sets.newConcurrentHashSet();

    private final ExecutorService executorService = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true
    );

    private final CompletionService<Boolean> executorCompletionService = new ExecutorCompletionService<>(executorService);

    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue;

    public WriterService(Set<AbstractWriter> writers, ArrayBlockingQueue<List<Metric>> arrayBlockingQueue) {
        this.writers = writers;
        this.arrayBlockingQueue = arrayBlockingQueue;
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.info("Initializing readers");
        Stopwatch stopwatch = Stopwatch.createStarted();

        for (AbstractWriter writer : writers) {
            try {
                LOGGER.info("Starting up {}", writer.getName());
                writer.startUp();

                LOGGER.info("{} enabled", writer.getName());
                enabledWriters.add(writer);
            } catch (Exception e) {
                LOGGER.warn("{} disabled, cause: {}", writer.getName(), e.getMessage());
            }
        }

        LOGGER.info("Readers initialized in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            List<Metric> metrics = arrayBlockingQueue.take();
            Stopwatch stopwatch = Stopwatch.createStarted();
            LOGGER.info("Writing metrics to writers");

            for (AbstractWriter writer : enabledWriters) {
                writer.setMetricList(metrics);
                executorCompletionService.submit(writer);
            }

            for (int i = 0; i < enabledWriters.size(); i++) {
                try {
                    executorCompletionService.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("An error occured while executing writer: {}", e.getMessage());
                }
            }

            LOGGER.info("Metrics written in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }


}
