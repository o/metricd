package org.polimat.metricd;

import java.util.List;
import java.util.concurrent.Callable;

abstract public class AbstractWriter implements Callable<Boolean> {

    private List<Metric> metricList;

    public List<Metric> getMetricList() {
        return metricList;
    }

    public AbstractWriter setMetricList(List<Metric> metricList) {
        this.metricList = metricList;
        return this;
    }

    @Override
    public final Boolean call() {
        return writeBatch();
    }

    abstract protected Boolean writeBatch();

    abstract public String getName();

    public void startUp() throws Exception {
    }

}
