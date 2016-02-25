package org.polimat.metricd;

import java.util.List;
import java.util.concurrent.Callable;

abstract public class AbstractReader implements Callable<List<Metric>>, Plugin {

    protected static final Integer REPORT_PERIOD = 10;

    @Override
    public final List<Metric> call() {
        return collect();
    }

    abstract protected List<Metric> collect();

}
