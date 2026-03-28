package Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Props {
    private static final Logger logger = LogManager.getLogger();

    public static Properties getProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = Props.class.getClassLoader().getResourceAsStream("bd.config")) {
            if (inputStream == null) {
                logger.error("Properties file not found.");
                return null;
            }
            properties.load(inputStream);
        } catch (IOException e) {
            logger.error("Error loading properties file: {}", e.getMessage());
        }
        return properties;
    }
}
