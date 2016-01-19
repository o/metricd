package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CollectorService extends AbstractScheduledService {

    private static final Integer REPORT_PERIOD = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduledService.class);

    private final ReaderManager readerManager;

    private final ExecutorService executorService = new ForkJoinPool(
            Runtime.getRuntime().availableProcessors(),
            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
            null,
            true
    );

    private final CompletionService<List<Metric>> executorCompletionService = new ExecutorCompletionService<>(executorService);

    public CollectorService(ReaderManager readerManager) {
        this.readerManager = readerManager;
    }

    @Override
    protected void runOneIteration() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("Getting metrics from readers");

        List<Metric> metricList = getMetricsFromReaders();
        LOGGER.info("Metrics collected in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        for (Metric metric : metricList) {
            LOGGER.info(metric.toString());
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, REPORT_PERIOD, TimeUnit.SECONDS);
    }

    private List<Metric> getMetricsFromReaders() {
        for (AbstractReader reader : readerManager.getEnabledReaders()) {
            executorCompletionService.submit(reader);
        }

        List<Metric> metricList = new ArrayList<>();

        for (int i = 0; i < readerManager.getEnabledReaders().size(); i++) {
            try {
                List<Metric> metrics = executorCompletionService.take().get();
                metricList.addAll(metrics);
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.error("An error occured while executing reader: {}", e.getMessage());
            }
        }

        LOGGER.info(executorService.toString());
        return metricList;
    }

}
