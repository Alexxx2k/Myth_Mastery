package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RouteRepository extends JpaRepository<RouteEntity, Integer> { }