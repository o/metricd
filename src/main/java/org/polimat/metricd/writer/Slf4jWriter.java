package org.polimat.metricd.writer;

import com.google.common.collect.Sets;
import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Metric;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.config.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

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
    public Set<Plugin> build(Configuration configuration) throws Exception {
        return Sets.newHashSet(this);
    }

    @Override
    public String getName() {
        return "SLF4J console";
    }

}
