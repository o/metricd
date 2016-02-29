package org.polimat.metricd.writer;

import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.httpserver.JettyServerInstantiatior;
import org.polimat.metricd.httpserver.JsonEndpointHandler;

public class JettyWriter extends AbstractWriter {

    private final JsonEndpointHandler jsonEndpointHandler = new JsonEndpointHandler();

    private final JettyServerInstantiatior jettyServerInstantiatior = new JettyServerInstantiatior(jsonEndpointHandler);

    private final Integer PORT;

    public JettyWriter(Integer PORT) {
        this.PORT = PORT;
    }

    @Override
    protected Boolean write() {
        return jsonEndpointHandler.replaceMetricList(getMetrics());
    }

    @Override
    public void startUp() throws Exception {
        jettyServerInstantiatior.startJetty(PORT);

    }

    @Override
    public String getName() {
        return "Jetty JSON";
    }
}
