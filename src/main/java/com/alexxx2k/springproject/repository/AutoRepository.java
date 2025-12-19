package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.AutoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoRepository extends JpaRepository<AutoEntity, Integer> {}