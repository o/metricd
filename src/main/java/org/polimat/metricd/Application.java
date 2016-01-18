package org.polimat.metricd;

import com.google.common.util.concurrent.ServiceManager;
import org.polimat.metricd.reader.linux.LoadAverage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Application {

    public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {

        List<AbstractReader> readers = new ArrayList<>();
        readers.add(new LoadAverage());

        CollectorService collectorService = new CollectorService(readers);
        collectorService.startAsync();
    }
}
