package org.polimat.metricd.config.node;

import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.reader.LoadAverageReader;

import java.util.Set;

public class LoadAverageNode extends AbstractNode {

    @Override
    protected Set<Plugin> build() {
        return Sets.newHashSet(new LoadAverageReader());
    }
}
