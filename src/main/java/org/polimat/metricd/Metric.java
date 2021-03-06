package org.polimat.metricd;

import com.google.common.base.MoreObjects;

public final class Metric<V> {

    private final String name;
    private final String key;
    private final V value;
    private final State state;
    private String description;

    public Metric(String name, String key, V value, State state, String description) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.state = state;
        this.description = description;
    }

    public Metric(String name, String key, V value) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.state = State.OK;
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public State getState() {
        return state;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("key", key)
                .add("value", value)
                .add("description", description)
                .add("state", state.getName())
                .toString();
    }
}
