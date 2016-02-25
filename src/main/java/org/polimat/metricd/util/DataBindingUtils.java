package org.polimat.metricd.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.polimat.metricd.Metric;
import org.polimat.metricd.config.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class DataBindingUtils {

    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());
    private static final ObjectWriter jsonWriter = new ObjectMapper().writerWithDefaultPrettyPrinter();

    public static Configuration readConfiguration(final File file) throws IOException {
        return yamlMapper.readValue(file, Configuration.class);
    }

    public static String writeMetrics(final List<Metric> metrics) throws JsonProcessingException {
        return jsonWriter.writeValueAsString(metrics);
    }
}
