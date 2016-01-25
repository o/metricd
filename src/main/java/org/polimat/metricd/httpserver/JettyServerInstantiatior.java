package org.polimat.metricd.httpserver;

import com.google.common.util.concurrent.AbstractIdleService;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.polimat.metricd.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JettyServerInstantiatior extends AbstractIdleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServerInstantiatior.class);

    private static final Integer PORT = 8092;

    private static final Long SHUTDOWN_GRACE_PERIOD = 5000L;

    private static final Long IDLE_TIMEOUT = 30000L;

    private static final String WEBAPP_FOLDER = "webapp";

    private static final String WEBAPP_RESOURCE_PATH = Application.class.getClassLoader().getResource(WEBAPP_FOLDER).toExternalForm();

    private static final Integer MIN_THREAD_SIZE = 1;

    private static final Integer MAX_THREAD_SIZE = 8;

    private static final Integer ACCEPT_BACKLOG = 20;

    private final JsonHandler jsonHandler;

    private final QueuedThreadPool queuedThreadPool = new QueuedThreadPool(MAX_THREAD_SIZE, MIN_THREAD_SIZE);

    private final Server server = new Server(queuedThreadPool);

    public JettyServerInstantiatior(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Override
    protected void startUp() throws Exception {
        LOGGER.info("Starting up Jetty HTTP server");
        try {
            final ServerConnector http = new ServerConnector(server);
            http.setPort(PORT);
            http.setIdleTimeout(IDLE_TIMEOUT);
            http.setAcceptQueueSize(ACCEPT_BACKLOG);

            server.addConnector(http);

            final ContextHandler jsonContext = new ContextHandler("/metrics.json");
            jsonContext.setHandler(jsonHandler);
            jsonContext.setAllowNullPathInfo(true);

            final ResourceHandler resourceHandler = new ResourceHandler();
            resourceHandler.setWelcomeFiles(new String[]{"index.html"});
            resourceHandler.setBaseResource(Resource.newResource(WEBAPP_RESOURCE_PATH));

            final HandlerList handlerList = new HandlerList();
            handlerList.setHandlers(new Handler[]{jsonContext, resourceHandler, new DefaultHandler()});

            server.setHandler(handlerList);
            server.setStopAtShutdown(true);
            server.setStopTimeout(SHUTDOWN_GRACE_PERIOD);

            server.start();
        } catch (Exception e) {
            LOGGER.error("A problem occured in Jetty HTTP server: {}", e.getMessage());
        }
    }

    @Override
    protected void shutDown() throws Exception {
        server.stop();
    }

}
