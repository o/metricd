package org.polimat.metricd.writer;

import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Metric;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsoleWriter extends AbstractWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleWriter.class);

    @Override
    protected Boolean write() {
        for (Metric metric : getMetrics()) {
            LOGGER.info(metric.toString());
        }
        return true;
    }

    @Override
    public String getName() {
        return "SLF4J console";
    }

}
