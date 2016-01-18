package org.polimat.metricd;

import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CollectorService extends AbstractScheduledService {

    public static final Logger LOGGER = LoggerFactory.getLogger(AbstractScheduledService.class);

    private final List<AbstractReader> readers;

    public CollectorService(List<AbstractReader> readers) {
        this.readers = readers;
    }

    @Override
    protected void runOneIteration() throws Exception {
        LOGGER.info("Getting metrics from readers");
        List<Metric> metricList = getMetricsFromReaders();

        for (Metric metric : metricList) {
            LOGGER.info(metric.toString());
        }
    }

    @Override
    protected Scheduler scheduler() {
        return Scheduler.newFixedRateSchedule(0, 10, TimeUnit.SECONDS);
    }

    private List<Metric> getMetricsFromReaders() {
        ExecutorService executorService = newWorkStealingPool();
        CompletionService<List<Metric>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        for (AbstractReader reader : readers) {
            executorCompletionService.submit(reader);
        }

        List<Metric> metricList = new ArrayList<>();

        for (int i = 0; i < readers.size(); i++) {
            try {
                List<Metric> metrics = executorCompletionService.take().get();
                metricList.addAll(metrics);
            } catch (InterruptedException e) {
                LOGGER.warn(e.getMessage());
            } catch (ExecutionException e) {
                LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
        }

        executorService.shutdown();
        LOGGER.info(executorService.toString());

        return metricList;
    }

    private ExecutorService newWorkStealingPool() {
        return new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true
        );
    }

}
