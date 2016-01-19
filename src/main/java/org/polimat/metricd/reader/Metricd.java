package org.polimat.metricd.reader;

import org.polimat.metricd.AbstractReader;
import org.polimat.metricd.Application;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class Metricd extends AbstractReader {

    private static final String UNKNOWN_HOSTNAME = "(none)";

    @Override
    protected List<Metric> getMetrics() {
        List<Metric> metrics = new ArrayList<>();

        String hostName = UNKNOWN_HOSTNAME;
        Calendar calendar = GregorianCalendar.getInstance();

        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // Ignore
        }

        metrics.add(new Metric<>("Version", "metricd/version", Application.VERSION));
        metrics.add(new Metric<>(
                "Last update time", "metricd/last-updated", System.currentTimeMillis(),
                State.OK,
                String.format("Last updated %s", new SimpleDateFormat("E HH:mm:ss").format(calendar.getTime()))
        ));

        metrics.add(new Metric<>(
                "Hostname", "metricd/hostname", 1,
                State.OK,
                hostName
        ));

        return metrics;
    }

    @Override
    public String getName() {
        return "Metadata";
    }


}
