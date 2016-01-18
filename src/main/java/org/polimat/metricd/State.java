package org.polimat.metricd;

public enum State {
    OK("ok"),
    WARNING("warn"),
    CRITICAL("crit");

    private final String name;

    State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
