package org.polimat.metricd.config.node;

import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.reader.MemoryUsage;

import java.util.Set;

public class MemoryUsageNode extends AbstractNode {

    @Override
    protected Set<Plugin> build() {
        return Sets.newHashSet(new MemoryUsage());
    }
}
