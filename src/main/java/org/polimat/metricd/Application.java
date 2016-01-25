package org.polimat.metricd;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ServiceManager;
import org.polimat.metricd.httpserver.JettyServerInstantiatior;
import org.polimat.metricd.httpserver.JsonHandler;
import org.polimat.metricd.reader.*;
import org.polimat.metricd.writer.JsonHandlerWriter;
import org.polimat.metricd.writer.Slf4jWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Application {

    public static final Double VERSION = 1.8;
    public static final String NAME = "metricd";
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.info("{} {} {}, {} {}, {}", System.getProperty("os.name"), System.getProperty("os.version"), System.getProperty("os.arch"), System.getProperty("java.runtime.name"), System.getProperty("java.runtime.version"), System.getProperty("java.vm.name"));
        LOGGER.info("{} {}, getting up and running..", NAME, VERSION);

        ArrayBlockingQueue<List<Metric>> arrayBlockingQueue = new ArrayBlockingQueue<>(10);

        ReaderService readerService = new ReaderService(
                Sets.newHashSet(
                        new LoadAverage(),
                        new DiskUsage(),
                        new Connections(),
                        new CpuUsage(),
                        new IOStats(),
                        new MemoryUsage(),
                        new NetworkUsage(),
                        new Metricd()
                ),
                arrayBlockingQueue
        );

        JsonHandler jsonHandler = new JsonHandler();
        JettyServerInstantiatior jettyServerInstantiatior = new JettyServerInstantiatior(jsonHandler);

        WriterService writerService = new WriterService(
                Sets.newHashSet(
                        new JsonHandlerWriter(jsonHandler),
                        new Slf4jWriter()
                ),
                arrayBlockingQueue
        );


        ServiceManager serviceManager = new ServiceManager(Sets.newHashSet(
                readerService,
                writerService,
                jettyServerInstantiatior
        ));

        serviceManager.startAsync();
    }
}
