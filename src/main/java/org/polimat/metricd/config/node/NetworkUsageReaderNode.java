package org.polimat.metricd.config.node;

import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.reader.NetworkUsageReader;

import java.util.Set;

public class NetworkUsageReaderNode extends AbstractNode {

    @Override
    protected Set<Plugin> build() {
        return Sets.newHashSet(new NetworkUsageReader());
    }
}
