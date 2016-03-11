package org.polimat.metricd;

import com.google.common.base.Stopwatch;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class WriterService extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriterService.class);

    private static final Integer TIMEOUT = 4;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    private final Set<AbstractWriter> writers;

    private final CompletionService<Boolean> executorCompletionService = new ExecutorCompletionService<>(ForkJoinPool.commonPool());

    private final ArrayBlockingQueue<List<Metric>> arrayBlockingQueue;

    public WriterService(Set<AbstractWriter> writers, ArrayBlockingQueue<List<Metric>> arrayBlockingQueue) {
        this.writers = writers;
        this.arrayBlockingQueue = arrayBlockingQueue;
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            List<Metric> metrics = arrayBlockingQueue.take();
            Stopwatch stopwatch = Stopwatch.createStarted();
            LOGGER.info("Writing metrics to writers");

            for (AbstractWriter writer : writers) {
                writer.setMetrics(metrics);
                executorCompletionService.submit(writer);
            }

            for (int i = 0; i < writers.size(); i++) {
                try {
                    Future<Boolean> future = executorCompletionService.poll(TIMEOUT, TIMEOUT_UNIT);
                    if (null != future) {
                        future.get();
                    }
                } catch (ExecutionException e) {
                    LOGGER.error("An error occured while executing writer: {}", e.getMessage());
                } catch (InterruptedException e) {
                    LOGGER.error("A reader was interrupted due to timeout: {}", e.getMessage());
                }
            }

            LOGGER.info("Metrics written in {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));
        }
    }


}
