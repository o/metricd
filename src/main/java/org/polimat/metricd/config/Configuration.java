package org.polimat.metricd.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.config.node.*;
import org.polimat.metricd.reader.Metricd;

import java.util.Set;

public class Configuration {

    @JsonProperty
    private ConsoleNode console;

    @JsonProperty
    private JettyNode jetty;

    @JsonProperty
    private ConnectionsNode connections;

    @JsonProperty
    private CpuUsageNode cpu;

    @JsonProperty
    private DiskUsageNode disk;

    @JsonProperty
    private IOStatsNode io;

    @JsonProperty
    private LoadAverageNode load;

    @JsonProperty
    private MemoryUsageNode memory;

    @JsonProperty
    private NetworkUsageNode network;

    @JsonIgnore
    public Set<Plugin> getPlugins() {
        Set<Plugin> plugins = Sets.newHashSet();

        plugins.add(new Metricd());
        plugins.addAll(connections.buildIfEnabled());
        plugins.addAll(cpu.buildIfEnabled());
        plugins.addAll(disk.buildIfEnabled());
        plugins.addAll(io.buildIfEnabled());
        plugins.addAll(load.buildIfEnabled());
        plugins.addAll(memory.buildIfEnabled());
        plugins.addAll(network.buildIfEnabled());
        plugins.addAll(console.buildIfEnabled());
        plugins.addAll(jetty.buildIfEnabled());

        return plugins;
    }


}


