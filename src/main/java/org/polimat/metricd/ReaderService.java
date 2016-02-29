package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractScheduledService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class ReaderService extends AbstractScheduledService {

    private static final Integer REPORT_PERIOD = 10;

    private static final Logger LOGGER = LoggerFactory.getLogger(ReaderService.class);

    private static final Integer TIMEOUT = 4;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Set<AbstractReader> readers;

    private final CompletionService<List<Metric>> executorCompletionService = new ExecutorCompletionService<>(ForkJoinPool.commonPool());

    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue;

    public ReaderService(Set<AbstractReader> readers, ArrayBlockingQueue<List<Metric>> arrayBlockingQueue) {
        this.readers = readers;
        this.arrayBlockingQueue = arrayBlockingQueue;
    }

    @Override
    protected void runOneIteration() throws Exception {
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOGGER.info("Getting metrics from readers");

        for (AbstractReader reader : readers) {
            executorCompletionService.submit(reader);
        }

        List<Metric> metricList = new ArrayList<>();

        for (int i = 0; i < readers.size(); i++) {
            try {
                Future<List<Metric>> future = executorCompletionService.poll(TIMEOUT, TIMEOUT_UNIT);
                if (null != future) {
                    List<Metric> metrics = future.get();
                    metricList.addAll(metrics);
                }
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
