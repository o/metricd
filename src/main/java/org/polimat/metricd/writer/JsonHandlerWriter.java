package org.polimat.metricd.writer;

import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.httpserver.JsonHandler;

public class JsonHandlerWriter extends AbstractWriter {

    private final JsonHandler jsonHandler;

    public JsonHandlerWriter(JsonHandler jsonHandler) {
        this.jsonHandler = jsonHandler;
    }

    @Override
    protected Boolean writeBatch() {
        return jsonHandler.replaceMetricList(getMetricList());
    }

    @Override
    public String getName() {
        return "Json handler writer";
    }
}
