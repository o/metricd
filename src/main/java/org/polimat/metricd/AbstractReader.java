package org.polimat.metricd;

import java.util.List;
import java.util.concurrent.Callable;

abstract public class AbstractReader implements Callable<List<Metric>> {

    @Override
    public final List<Metric> call() {
        return getMetrics();
    }

    private boolean isEnabled;

    abstract public List<Metric> getMetrics();

    abstract public void startUp();

    abstract public String getName();

    public boolean isEnabled() {
        return isEnabled;
    }

    public AbstractReader setEnabled(boolean enabled) {
        isEnabled = enabled;
        return this;
    }
}
