package com.alexxx2k.springproject.deliveryTimeCalc;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

class DeliveryTimeCalculatorSimpleTest {

    @Test
    void getMinutes_ShouldReturnNull_WhenException() throws Exception {
        try (MockedStatic<DGISGeocoder> geocoderMock = Mockito.mockStatic(DGISGeocoder.class)) {

            geocoderMock.when(() -> DGISGeocoder.getCoordinates(anyString(), anyString()))
                    .thenThrow(new RuntimeException("Ошибка геокодера"));

            Long result = DeliveryTimeCalculator.getMinutes("Москва");

            assertNull(result);
        }
    }
}
