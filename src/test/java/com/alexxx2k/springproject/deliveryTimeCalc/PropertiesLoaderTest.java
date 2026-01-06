package com.alexxx2k.springproject.deliveryTimeCalc;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PropertiesLoaderTest {

    @Test
    void get2GisApiKey_ShouldReturnKey() {
        PropertiesLoader loader = new PropertiesLoader();

        String key = loader.get2GisApiKey();

        assertNotNull(key);
    }
}
