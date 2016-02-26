package org.polimat.metricd;

import org.polimat.metricd.config.Configuration;

import java.util.Set;

public interface Plugin {

    String getName();

    Set<Plugin> build(Configuration configuration) throws Exception;

}
