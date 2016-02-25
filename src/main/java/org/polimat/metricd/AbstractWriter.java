package org.polimat.metricd;

import java.util.List;
import java.util.concurrent.Callable;

abstract public class AbstractWriter implements Callable<Boolean>, Plugin {

    private List<Metric> metrics;

    public List<Metric> getMetrics() {
        return metrics;
    }

    public AbstractWriter setMetrics(List<Metric> metrics) {
        this.metrics = metrics;
        return this;
    }

    @Override
    public final Boolean call() {
        return write();
    }

    abstract protected Boolean write();

}
