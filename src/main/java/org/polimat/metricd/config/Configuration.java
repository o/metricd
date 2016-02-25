package org.polimat.metricd.config;

import org.polimat.metricd.config.node.ServerNode;

public class Configuration {

    private ServerNode server;

    public ServerNode getServer() {
        return server;
    }

    public Configuration setServer(ServerNode server) {
        this.server = server;
        return this;
    }
}


