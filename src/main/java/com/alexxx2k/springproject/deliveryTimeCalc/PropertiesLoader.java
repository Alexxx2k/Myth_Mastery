package com.alexxx2k.springproject.deliveryTimeCalc;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {

    private static final Logger logger = LogManager.getLogger(PropertiesLoader.class);
    private final Properties properties = new Properties();

    public PropertiesLoader() {
        try (InputStream input = getClass().getResourceAsStream("/application.properties")) {
            if (input == null) {
                logger.error("Файл application.properties не найден");
                return;
            }
            properties.load(input);
        } catch (IOException e) {
            logger.error("Ошибка при загрузке application.properties");
            e.printStackTrace();
        }
    }

    public String get2GisApiKey() {
        return properties.getProperty("apiKey.2gis");
    }
}
