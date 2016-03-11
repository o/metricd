package org.polimat.metricd.writer;

import com.aphyr.riemann.Proto;
import com.aphyr.riemann.client.EventDSL;
import com.aphyr.riemann.client.RiemannClient;
import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class RiemannWriter extends AbstractWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiemannWriter.class);

    private static final Float TTL = 20f;

    private RiemannClient riemannClient;

    private final String host;

    private final Integer port;

    public RiemannWriter(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public String getName() {
        return String.format("Riemann [%s %s]", host, port);
    }

    @Override
    public void startUp() throws Exception {
        riemannClient = RiemannClient.tcp(host, port);
    }

    @Override
    protected Boolean write() {
        List<Proto.Event> eventList = new ArrayList<>();

        for (Metric metric : getMetrics()) {
            EventDSL event = riemannClient.event();
            event.service(metric.getKey())
                    .state(metric.getState().getName())
                    .description(metric.getDescription())
                    .ttl(TTL);

            Object value = metric.getValue();

            if (value instanceof Float) {
                event.metric((Float) value);
            } else if (value instanceof Double) {
                event.metric((Double) value);
            } else if (value instanceof Byte) {
                event.metric((Byte) value);
            } else if (value instanceof Short) {
                event.metric((Short) value);
            } else if (value instanceof Integer) {
                event.metric((Integer) value);
            } else if (value instanceof Long) {
                event.metric((Long) value);
            } else {
                break; // null or non-Number object
            }

            eventList.add(event.build());
        }

        eventList.add(riemannClient
                .event()
                .service("metricd/events")
                .state(State.OK.getName())
                .ttl(TTL)
                .metric(eventList.size()).build());

        try {
            if (!riemannClient.isConnected()) {
                LOGGER.info("Connecting to server {}:{}", host, port);
                riemannClient.connect();
            }
            LOGGER.info("Trying to send {} metric", eventList.size());
            riemannClient.sendEvents(eventList);
            return true;
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            riemannClient.close();
        }

        return false;
    }

}
