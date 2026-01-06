package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.City;
import com.alexxx2k.springproject.domain.entities.CityEntity;
import com.alexxx2k.springproject.repository.CityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CityServiceTest {

    @Mock
    private CityRepository cityRepository;

    @InjectMocks
    private CityService cityService;

    private CityEntity testEntity;
    private City testCity;

    @BeforeEach
    void setUp() {
        testEntity = new CityEntity(1L, "Москва", 5L);
        testCity = new City(1L, "Москва", 5L);
    }

    @Test
    void getAllCities_ShouldReturnCities() {
        when(cityRepository.findAll()).thenReturn(List.of(testEntity));

        List<City> cities = cityService.getAllCities();

        assertEquals(1, cities.size());
        assertEquals("Москва", cities.get(0).name());
        verify(cityRepository, times(1)).findAll();
    }

    @Test
    void getCityById_ShouldReturnCity() {
        when(cityRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<City> result = cityService.getCityById(1L);

        assertTrue(result.isPresent());
        assertEquals("Москва", result.get().name());
    }

    @Test
    void getCityById_ShouldReturnEmptyWhenNotFound() {
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<City> result = cityService.getCityById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createCity_ShouldCreateCity() {
        try (MockedStatic<com.alexxx2k.springproject.deliveryTimeCalc.DeliveryTimeCalculator> mockedCalculator =
                     Mockito.mockStatic(com.alexxx2k.springproject.deliveryTimeCalc.DeliveryTimeCalculator.class)) {

            mockedCalculator.when(() ->
                            com.alexxx2k.springproject.deliveryTimeCalc.DeliveryTimeCalculator.getMinutes(anyString()))
                    .thenReturn(2400L);

            when(cityRepository.existsByName("Москва")).thenReturn(false);
            when(cityRepository.save(any(CityEntity.class))).thenReturn(testEntity);

            City result = cityService.createCity(new City(null, "Москва", null));

            assertNotNull(result);
            assertEquals("Москва", result.name());
            verify(cityRepository, times(1)).save(any(CityEntity.class));
        }
    }

    @Test
    void createCity_ShouldThrowWhenCityExists() {
        when(cityRepository.existsByName("Москва")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            cityService.createCity(testCity);
        });
    }

    @Test
    void updateCity_ShouldThrowWhenCityWithSameNameExists() {
        CityEntity anotherEntity = new CityEntity(2L, "Москва", 3L);

        when(cityRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(cityRepository.findByName("Москва")).thenReturn(Optional.of(anotherEntity));

        City updatedCity = new City(1L, "Москва", 7L);

        assertThrows(IllegalArgumentException.class, () -> {
            cityService.updateCity(1L, updatedCity);
        });
    }

    @Test
    void updateCity_ShouldThrowWhenNotFound() {
        when(cityRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            cityService.updateCity(1L, testCity);
        });
    }

    @Test
    void deleteCity_ShouldDeleteCity() {
        when(cityRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cityRepository).deleteById(1L);

        cityService.deleteCity(1L);

        verify(cityRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCity_ShouldThrowWhenNotFound() {
        when(cityRepository.existsById(1L)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> {
            cityService.deleteCity(1L);
        });
    }

    @Test
    void existsByName_ShouldReturnTrue() {
        when(cityRepository.existsByName("Москва")).thenReturn(true);

        boolean exists = cityService.existsByName("Москва");

        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalse() {
        when(cityRepository.existsByName("Москва")).thenReturn(false);

        boolean exists = cityService.existsByName("Москва");

        assertFalse(exists);
    }
}
