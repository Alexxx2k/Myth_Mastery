package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MythologyRepository extends JpaRepository<MythologyEntity, Long> {
    boolean existsByName(String name);

    @Query("SELECT m FROM MythologyEntity m WHERE m.name = :name")
    Optional<MythologyEntity> findByName(@Param("name") String name);
}
