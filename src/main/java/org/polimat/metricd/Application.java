package org.polimat.metricd;

import com.google.common.util.concurrent.ServiceManager;
import org.polimat.metricd.config.Configuration;
import org.polimat.metricd.util.DataBindingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

public class Application {

    public static final String VERSION = "2.0-SNAPSHOT";
    public static final String NAME = "metricd";
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        LOGGER.info("{} {} starting", NAME, VERSION);

        Configuration configuration = new Configuration();

        try {
            configuration = DataBindingUtils.readConfiguration(new File("config.yml"));
        } catch (IOException e) {
            LOGGER.error("Unable to read configuration, exiting.");
            LOGGER.error(e.getMessage());
            System.exit(1);
        }

        final ConfigurationAwareServiceFactory configurationAwareServiceFactory = new ConfigurationAwareServiceFactory(configuration);
        configurationAwareServiceFactory.initializePlugins();

        final ServiceManager serviceManager = new ServiceManager(configurationAwareServiceFactory.getServices());
        LOGGER.info("Starting services");
        serviceManager.startAsync();
    }
}
