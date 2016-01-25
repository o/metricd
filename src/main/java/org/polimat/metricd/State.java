package org.polimat.metricd;

public enum State {
    OK("ok"),
    WARNING("warning"),
    CRITICAL("critical");

    private final String name;

    State(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
