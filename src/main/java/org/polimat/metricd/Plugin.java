package org.polimat.metricd;

public interface Plugin {

    String getName();

    default void startUp() throws Exception {
    }

}
