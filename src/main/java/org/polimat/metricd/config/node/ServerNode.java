package org.polimat.metricd.config.node;

public class ServerNode {

    private Boolean enabled;

    private Integer port;

    public Boolean getEnabled() {
        return enabled;
    }

    public ServerNode setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public ServerNode setPort(Integer port) {
        this.port = port;
        return this;
    }
}
