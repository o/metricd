package org.polimat.metricd.config.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.writer.JettyWriter;

import java.util.Set;

public class JettyWriterNode extends AbstractNode {

    @JsonProperty
    private Integer port;

    public Integer getPort() {
        return port;
    }

    public JettyWriterNode setPort(Integer port) {
        this.port = port;
        return this;
    }

    @Override
    protected Set<Plugin> build() {
        Preconditions.checkNotNull(getPort(), "Missing port property in Jetty Writer configuation");
        return Sets.newHashSet(new JettyWriter(getPort()));
    }
}
