package org.polimat.metricd.writer;

import com.google.common.collect.Sets;
import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.httpserver.JettyServerInstantiatior;
import org.polimat.metricd.httpserver.JsonEndpointHandler;

import java.util.Set;

public class JettyWriter extends AbstractWriter {

    private final JsonEndpointHandler jsonEndpointHandler = new JsonEndpointHandler();

    private final JettyServerInstantiatior jettyServerInstantiatior = new JettyServerInstantiatior(jsonEndpointHandler);

    @Override
    protected Boolean write() {
        return jsonEndpointHandler.replaceMetricList(getMetrics());
    }

    @Override
    public Set<Plugin> build(Configuration configuration) throws Exception {
        if (!configuration.getServer().getEnabled()) {
            throw new Exception("Not enabled");
        }
        jettyServerInstantiatior.startJetty(configuration.getServer().getPort());

        return Sets.newHashSet(this);
    }

    @Override
    public String getName() {
        return "Jetty JSON";
    }
}
