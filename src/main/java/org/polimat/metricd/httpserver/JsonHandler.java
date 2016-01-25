package org.polimat.metricd.httpserver;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.ajax.JSONEnumConvertor;
import org.eclipse.jetty.util.ajax.JSONObjectConvertor;
import org.polimat.metricd.Application;
import org.polimat.metricd.Metric;
import org.polimat.metricd.State;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class JsonHandler extends AbstractHandler {

    private final List<Metric> metricList = new CopyOnWriteArrayList<>();

    private final JSON jsonConverter;

    public JsonHandler() {
        jsonConverter = new JSON();
        jsonConverter.addConvertor(Metric.class, new JSONObjectConvertor());
        jsonConverter.addConvertor(State.class, new JSONEnumConvertor());
    }

    public Boolean replaceMetricList(List<Metric> metrics) {
        metricList.clear();
        return metricList.addAll(metrics);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("application/json; charset=utf-8");
        response.addHeader("X-Powered-By", Application.NAME);
        response.setStatus(HttpServletResponse.SC_OK);

        PrintWriter out = response.getWriter();
        out.println(jsonConverter.toJSON(metricList));

        baseRequest.setHandled(true);
    }

}
