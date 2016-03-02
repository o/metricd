package org.polimat.metricd.config.node;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.writer.RiemannWriter;

import java.util.Set;

public class RiemannWriterNode extends AbstractNode {

    private String host;

    private Integer port;

    public String getHost() {
        return host;
    }

    public RiemannWriterNode setHost(String host) {
        this.host = host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public RiemannWriterNode setPort(Integer port) {
        this.port = port;
        return this;
    }

    @Override
    protected Set<Plugin> build() {
        Preconditions.checkNotNull(getHost(), "Missing host property in Riemann Writer configuation");
        Preconditions.checkNotNull(getPort(), "Missing port property in Riemann Writer configuation");
        return Sets.newHashSet(new RiemannWriter(getHost(), getPort()));
    }
}
