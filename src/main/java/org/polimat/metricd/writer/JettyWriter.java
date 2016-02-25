package org.polimat.metricd.writer;

import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.httpserver.JettyServerInstantiatior;
import org.polimat.metricd.httpserver.JsonEndpointHandler;

public class JettyWriter extends AbstractWriter {

    private final JsonEndpointHandler jsonEndpointHandler = new JsonEndpointHandler();

    private final JettyServerInstantiatior jettyServerInstantiatior = new JettyServerInstantiatior(jsonEndpointHandler);

    @Override
    protected Boolean write() {
        return jsonEndpointHandler.replaceMetricList(getMetrics());
    }

    @Override
    public void startUp(Configuration configuration) throws Exception {
        if (!configuration.getServer().getEnabled()) {
            throw new Exception("Not enabled");
        }
        jettyServerInstantiatior.startJetty(configuration.getServer().getPort());

    }

    @Override
    public String getName() {
        return "Jetty JSON";
    }
}
