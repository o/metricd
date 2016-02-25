package org.polimat.metricd.writer;

import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Slf4jWriter extends AbstractWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jWriter.class);

    @Override
    protected Boolean write() {
        for (Metric metric : getMetrics()) {
            if (null != metric.getDescription()) {
                LOGGER.info("{}, {}, {}, {}", metric.getName(), metric.getState(), metric.getValue(), metric.getDescription());
            } else {
                LOGGER.info("{}, {}, {}", metric.getName(), metric.getState(), metric.getValue());
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return "SLF4J console";
    }

}
