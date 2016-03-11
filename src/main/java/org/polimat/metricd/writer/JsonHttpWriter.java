package org.polimat.metricd.writer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.polimat.metricd.AbstractWriter;
import org.polimat.metricd.Application;
import org.polimat.metricd.util.DataBindingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class JsonHttpWriter extends AbstractWriter {

    private static final List<Integer> VALID_STATUS_CODES = Lists.newArrayList(200, 201);

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonHttpWriter.class);

    private final String url;

    private final Map<String, String> headers;

    private HttpClient httpClient;

    private URI postUri;

    public JsonHttpWriter(String url, Map<String, String> headers) {
        this.url = url;
        this.headers = headers;
    }

    @Override
    protected Boolean write() {
        HttpPost post = new HttpPost(postUri);

        try {
            StringEntity jsonString = new StringEntity(DataBindingUtils.writeMetrics(getMetrics()), Charsets.UTF_8);
            post.setEntity(jsonString);
        } catch (JsonProcessingException e) {
            LOGGER.error(e.getMessage());
            return false;
        }

        try {
            LOGGER.info("Trying to send metrics to {}", url);
            HttpResponse response = httpClient.execute(post);
            return VALID_STATUS_CODES.contains(response.getStatusLine().getStatusCode());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            post.releaseConnection();
        }

        return false;
    }

    @Override
    public String getName() {
        return String.format("Json Http [%s]", url);
    }

    @Override
    public void startUp() throws Exception {
        // Checks syntax
        postUri = new URI(url);

        List<Header> defaultHeaders = Lists.newArrayList(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
        if (headers != null && headers.size() > 0) {
            for (Map.Entry<String, String> header : headers.entrySet()) {
                defaultHeaders.add(new BasicHeader(header.getKey(), header.getValue()));
            }
        }

        String userAgent = new StringBuilder(Application.NAME).append("/").append(Application.VERSION).toString();
        httpClient = HttpClientBuilder.create().setDefaultHeaders(defaultHeaders).setUserAgent(userAgent).build();
    }
}