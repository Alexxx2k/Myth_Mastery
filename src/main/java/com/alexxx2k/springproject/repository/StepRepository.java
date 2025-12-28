package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.StepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StepRepository extends JpaRepository<StepEntity, Long> {

    boolean existsByName(String name);

    @Query("SELECT s FROM StepEntity s WHERE s.name = :name")
    Optional<StepEntity> findByName(@Param("name") String name);
}
