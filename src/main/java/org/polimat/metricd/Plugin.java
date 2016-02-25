package org.polimat.metricd;

import org.polimat.metricd.config.Configuration;

public interface Plugin {

    String getName();

    default void startUp(Configuration configuration) throws Exception {
    }

}
