package org.polimat.metricd.reader;

import org.apache.commons.io.FileUtils;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;
import org.polimat.metricd.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Connections extends AbstractReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(Connections.class);

    private static final String FILENAME_PROC_NET_TCP = "/proc/net/tcp";
    private static final String FILENAME_PROC_NET_UDP = "/proc/net/udp";
    private static final String FILENAME_PROC_NET_TCP6 = "/proc/net/tcp6";
    private static final String FILENAME_PROC_NET_UDP6 = "/proc/net/udp6";

    private final File tcpFile = new File(FILENAME_PROC_NET_TCP);
    private final File udpFile = new File(FILENAME_PROC_NET_UDP);
    private final File tcp6File = new File(FILENAME_PROC_NET_TCP6);
    private final File udp6File = new File(FILENAME_PROC_NET_UDP6);

    @Override
    public List<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>();

        Long tcpConnections;
        Long udpConnections;
        Long tcp6Connections;
        Long udp6Connections;
        Long totalConnections;

        try {
            tcpConnections = getTcpConnectionsFileLines().size() - 1L;
            udpConnections = getUdpConnectionsFileLines().size() - 1L;
            tcp6Connections = getTcp6ConnectionsFileLines().size() - 1L;
            udp6Connections = getUdp6ConnectionsFileLines().size() - 1L;
            totalConnections = (tcpConnections + udpConnections + tcp6Connections + udp6Connections);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return metrics;
        }

        metrics.add(new Metric<>(
                "Total connections",
                "metricd/connections/total",
                totalConnections,
                State.OK,
                String.format(
                        "Current connections, tcp: %d, udp: %d, tcp6: %d, udp6: %d",
                        tcpConnections, udpConnections, tcp6Connections, udp6Connections
                )
        ));

        metrics.add(new Metric<>("TCP connections", "metricd/connections/tcp", tcpConnections));
        metrics.add(new Metric<>("UDP connections", "metricd/connections/udp", udpConnections));
        metrics.add(new Metric<>("TCP6 connections", "metricd/connections/tcp6", tcp6Connections));
        metrics.add(new Metric<>("UDP6 connections", "metricd/connections/udp6", udp6Connections));

        return metrics;
    }

    @Override
    public String getName() {
        return "Connections";
    }

    @Override
    public void startUp() throws Exception {
        IOUtils.checkFile(tcpFile);
    }

    private List<String> getTcpConnectionsFileLines() throws IOException {
        return FileUtils.readLines(tcpFile);
    }

    private List<String> getUdpConnectionsFileLines() throws IOException {
        return FileUtils.readLines(udpFile);
    }

    private List<String> getTcp6ConnectionsFileLines() throws IOException {
        return FileUtils.readLines(tcp6File);
    }

    private List<String> getUdp6ConnectionsFileLines() throws IOException {
        return FileUtils.readLines(udp6File);
    }

}
