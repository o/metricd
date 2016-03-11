package org.polimat.metricd.config.node;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import org.polimat.metricd.Plugin;
import org.polimat.metricd.writer.JsonHttpWriter;

import java.util.Map;
import java.util.Set;

public class JsonHttpWriterNode extends AbstractNode {

    @JsonProperty
    private String url;

    @JsonProperty
    private Map<String, String> headers;

    public String getUrl() {
        return url;
    }

    public JsonHttpWriterNode setUrl(String url) {
        this.url = url;
        return this;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public JsonHttpWriterNode setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    @Override
    protected Set<Plugin> build() {
        Preconditions.checkNotNull(getUrl(), "Missing url property in Json HTTP Writer configuation");
        return Sets.newHashSet(new JsonHttpWriter(getUrl(), getHeaders()));
    }

}
