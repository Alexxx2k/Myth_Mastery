package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.City;
import com.alexxx2k.springproject.domain.entities.CityEntity;
import com.alexxx2k.springproject.repository.CityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CityService {

    private final CityRepository cityRepository;

    public CityService(CityRepository cityRepository) {
        this.cityRepository = cityRepository;
    }

    public List<City> getAllCities() {
        return cityRepository.findAll().stream()
                .map(this::toDomainCity)
                .toList();
    }

    public Optional<City> getCityById(Long id) {
        return cityRepository.findById(id)
                .map(this::toDomainCity);
    }

    @Transactional
    public City createCity(City city) {
        if (cityRepository.existsByName(city.name())) {
            throw new IllegalArgumentException("Город с названием '" + city.name() + "' уже существует");
        }

        CityEntity entity = new CityEntity(null, city.name(), city.deliveryTime());
        CityEntity savedEntity = cityRepository.save(entity);
        return toDomainCity(savedEntity);
    }

    @Transactional
    public City updateCity(Long id, City city) {
        CityEntity existingEntity = cityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Город с ID " + id + " не найден"));

        Optional<CityEntity> duplicate = cityRepository.findByName(city.name());
        if (duplicate.isPresent() && !duplicate.get().getId().equals(id)) {
            throw new IllegalArgumentException("Город с названием '" + city.name() + "' уже существует");
        }

        existingEntity.setName(city.name());
        existingEntity.setDeliveryTime(city.deliveryTime());
        CityEntity savedEntity = cityRepository.save(existingEntity);
        return toDomainCity(savedEntity);
    }

    @Transactional
    public void deleteCity(Long id) {
        if (!cityRepository.existsById(id)) {
            throw new IllegalArgumentException("Город с ID " + id + " не найден");
        }
        cityRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return cityRepository.existsByName(name);
    }

    private City toDomainCity(CityEntity entity) {
        return new City(entity.getId(), entity.getName(), entity.getDeliveryTime());
    }
}
