package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.CityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CityRepository extends JpaRepository<CityEntity, Long> {
    boolean existsByName(String name);

    @Query("SELECT c FROM CityEntity c WHERE c.name = :name")
    Optional<CityEntity> findByName(@Param("name") String name);
}
