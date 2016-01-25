package org.polimat.metricd.reader;

import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Application;
import org.polimat.metricd.Metric;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Metricd extends AbstractReader {

    private static final String UNKNOWN_HOSTNAME = "(none)";

    private final String OS = System.getProperty("os.name");

    private final String OS_VERSION = System.getProperty("os.version");

    @Override
    protected List<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>();

        String hostName = UNKNOWN_HOSTNAME;

        Calendar calendar = GregorianCalendar.getInstance();

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException ignored) {
            //
        }

        Map<String, String> metadata = new HashMap<>();
        metadata.put("lastUpdated", new SimpleDateFormat("E HH:mm:ss").format(calendar.getTime()));
        metadata.put("hostName", hostName);
        metadata.put("operatingSystem", OS);
        metadata.put("operationSystemVersion", OS_VERSION);
        metadata.put("timestamp", String.valueOf(System.currentTimeMillis()));

        metrics.add(new Metric<>("Version", "metricd/version", Application.VERSION));
        metrics.add(new Metric<>("Metadata", "metricd/metadata", metadata));

        return metrics;
    }

    @Override
    public String getName() {
        return "Metadata";
    }


}
