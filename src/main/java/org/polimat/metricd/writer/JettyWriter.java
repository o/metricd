package org.polimat.metricd.writer;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Application;
import org.polimat.metricd.Metric;
import org.polimat.metricd.util.DataBindingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JettyWriter extends AbstractWriter {

    private static final Long SHUTDOWN_GRACE_PERIOD = 5000L;

    private static final Long IDLE_TIMEOUT = 30000L;

    private static final String WEBAPP_FOLDER = "webapp";

    private static final String WEBAPP_RESOURCE_PATH = Application.class.getClassLoader().getResource(WEBAPP_FOLDER).toExternalForm();

    private static final Integer MIN_THREAD_SIZE = 1;

    private static final Integer MAX_THREAD_SIZE = 6;

    private static final Integer ACCEPT_BACKLOG = 20;

    private static final String JSON_ENDPOINT = "/metrics.json";

    private static final String WELCOME_FILE = "index.html";

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyWriter.class);

    private final Integer port;

    private final JsonEndpointHandler jsonEndpointHandler = new JsonEndpointHandler();

    private final QueuedThreadPool queuedThreadPool = new QueuedThreadPool(MAX_THREAD_SIZE, MIN_THREAD_SIZE);

    private final Server server = new Server(queuedThreadPool);

    public JettyWriter(Integer port) {
        this.port = port;
    }

    @Override
    protected Boolean write() {
        return jsonEndpointHandler.replaceMetricList(getMetrics());
    }

    @Override
    public void startUp() throws Exception {
        LOGGER.info("Starting up Jetty HTTP server");
        final ServerConnector http = new ServerConnector(server);
        http.setPort(port);
        http.setIdleTimeout(IDLE_TIMEOUT);
        http.setAcceptQueueSize(ACCEPT_BACKLOG);

        server.addConnector(http);

        final ContextHandler jsonContext = new ContextHandler(JSON_ENDPOINT);
        jsonContext.setHandler(jsonEndpointHandler);
        jsonContext.setAllowNullPathInfo(true);

        final ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setWelcomeFiles(new String[]{WELCOME_FILE});
        resourceHandler.setBaseResource(Resource.newResource(WEBAPP_RESOURCE_PATH));

        final HandlerList handlerList = new HandlerList();
        handlerList.setHandlers(new Handler[]{jsonContext, resourceHandler, new DefaultHandler()});

        server.setHandler(handlerList);
        server.setStopAtShutdown(true);
        server.setStopTimeout(SHUTDOWN_GRACE_PERIOD);

        server.start();
        //server.join();
    }

    @Override
    public String getName() {
        return String.format("Jetty server [port %d]", port);
    }

    public class JsonEndpointHandler extends AbstractHandler {

        private final List<Metric> metricList = new CopyOnWriteArrayList<>();

        public Boolean replaceMetricList(List<Metric> metrics) {
            metricList.clear();
            return metricList.addAll(metrics);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            response.setContentType("application/json; charset=utf-8");
            response.addHeader("X-Powered-By", Application.NAME);
            response.setStatus(HttpServletResponse.SC_OK);

            final PrintWriter out = response.getWriter();
            out.println(DataBindingUtils.writeMetrics(metricList));

            baseRequest.setHandled(true);
        }

    }

}
