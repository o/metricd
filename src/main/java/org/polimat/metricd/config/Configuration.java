package org.polimat.metricd.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.config.node.*;
import org.polimat.metricd.reader.MetadataReader;

import java.util.Set;

public class Configuration {

    @JsonProperty
    private ConsoleWriterNode console;

    @JsonProperty
    private JettyWriterNode jetty;

    @JsonProperty
    private ConnectionsReaderNode connections;

    @JsonProperty
    private CpuUsageReaderNode cpu;

    @JsonProperty
    private DiskUsageReaderNode disk;

    @JsonProperty
    private LoadAverageReaderNode load;

    @JsonProperty
    private MemoryUsageReaderNode memory;

    @JsonProperty
    private NetworkUsageReaderNode network;

    @JsonProperty
    private RiemannWriterNode riemann;

    @JsonProperty
    private JsonHttpWriterNode http;

    @JsonIgnore
    public Set<Plugin> getPlugins() {
        Set<Plugin> plugins = Sets.newHashSet();

        plugins.add(new MetadataReader());
        plugins.addAll(connections.buildIfEnabled());
        plugins.addAll(cpu.buildIfEnabled());
        plugins.addAll(disk.buildIfEnabled());
        plugins.addAll(load.buildIfEnabled());
        plugins.addAll(memory.buildIfEnabled());
        plugins.addAll(network.buildIfEnabled());
        plugins.addAll(console.buildIfEnabled());
        plugins.addAll(jetty.buildIfEnabled());
        plugins.addAll(riemann.buildIfEnabled());
        plugins.addAll(http.buildIfEnabled());

        return plugins;
    }


}


