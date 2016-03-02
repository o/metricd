package org.polimat.metricd.reader;

import com.google.common.base.StandardSystemProperty;
import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Application;
import org.polimat.metricd.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataReader extends AbstractReader {

    private static final String UNKNOWN_HOSTNAME = "(none)";

    private final String OS = StandardSystemProperty.OS_NAME.value();

    private final String OS_VERSION = StandardSystemProperty.OS_VERSION.value();

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("E HH:mm:ss");

    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataReader.class);

    @Override
    protected List<Metric> collect() {
        List<Metric> metrics = new ArrayList<>();

        String hostName = UNKNOWN_HOSTNAME;
        String canonicalHostName = UNKNOWN_HOSTNAME;
        String hostAddress = null;

        try {
            InetAddress localhost = InetAddress.getLocalHost();
            hostName = localhost.getHostName();
            hostAddress = localhost.getHostAddress();
            canonicalHostName = localhost.getCanonicalHostName();
        } catch (UnknownHostException e) { // Why JVM can't catch this?
            LOGGER.error(e.getMessage());
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("lastUpdated", LocalDateTime.now().format(dateFormat));
        metadata.put("timestamp", Instant.now().toString());
        metadata.put("hostName", hostName);
        metadata.put("canonicalHostName", canonicalHostName);
        metadata.put("hostAddress", hostAddress);
        metadata.put("operatingSystem", OS);
        metadata.put("operationSystemVersion", OS_VERSION);

        metrics.add(new Metric<>("Version", "metricd/version", Application.VERSION));
        metrics.add(new Metric<>("Metadata", "metricd/metadata", metadata));

        return metrics;
    }

    @Override
    public String getName() {
        return "Metadata";
    }


}
