package org.polimat.metricd.config.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import org.polimat.metricd.Plugin;

import java.util.HashSet;
import java.util.Set;

abstract public class AbstractNode {

    @JsonProperty
    private Boolean enabled;

    @JsonIgnore
    abstract protected Set<Plugin> build();

    @JsonIgnore
    public final Set<Plugin> buildIfEnabled() {
        Preconditions.checkNotNull(getEnabled(), "Missing enabled property in config");
        if (getEnabled()) {
            return build();
        } else {
            return new HashSet<>();
        }
    }

    protected Boolean getEnabled() {
        return enabled;
    }

    protected AbstractNode setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

}
