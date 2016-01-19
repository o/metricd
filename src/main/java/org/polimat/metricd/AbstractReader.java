package org.polimat.metricd;

import java.util.List;
import java.util.concurrent.Callable;

abstract public class AbstractReader implements Callable<List<Metric>> {

    protected static final Integer REPORT_PERIOD = 10;

    @Override
    public final List<Metric> call() {
        return getMetrics();
    }

    abstract protected List<Metric> getMetrics();

    abstract public String getName();

    public void startUp() throws Exception {
    }

}
