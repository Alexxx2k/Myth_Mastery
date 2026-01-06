package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.CityEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CityRepositoryTest {

    @Autowired
    private CityRepository cityRepository;

    @Test
    void save_ShouldSaveCity() {
        CityEntity city = new CityEntity(null, "Москва", 5L);

        CityEntity saved = cityRepository.save(city);

        assertNotNull(saved.getId());
        assertEquals("Москва", saved.getName());
    }

    @Test
    void findById_ShouldFindCity() {
        CityEntity city = new CityEntity(null, "Москва", 5L);
        CityEntity saved = cityRepository.save(city);

        Optional<CityEntity> found = cityRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals("Москва", found.get().getName());
    }

    @Test
    void existsByName_ShouldReturnTrue() {
        CityEntity city = new CityEntity(null, "Москва", 5L);
        cityRepository.save(city);

        boolean exists = cityRepository.existsByName("Москва");

        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalse() {
        boolean exists = cityRepository.existsByName("Несуществующий");

        assertFalse(exists);
    }

    @Test
    void findByName_ShouldFindCity() {
        CityEntity city = new CityEntity(null, "Москва", 5L);
        cityRepository.save(city);

        Optional<CityEntity> found = cityRepository.findByName("Москва");

        assertTrue(found.isPresent());
        assertEquals("Москва", found.get().getName());
    }
}
