package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Route;
import com.alexxx2k.springproject.domain.entities.RouteEntity;
import com.alexxx2k.springproject.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(this::toDomainRoute)
                .toList();
    }

    public Optional<Route> getRouteById(Integer id) {
        return routeRepository.findById(id)
                .map(this::toDomainRoute);
    }

    public Route createRoute(Route route) {
        var entityToSave = new RouteEntity(null, route.name());
        var savedEntity = routeRepository.save(entityToSave);
        return toDomainRoute(savedEntity);
    }

    @Transactional
    public Route updateRoute(Integer id, Route route) {
        RouteEntity existingEntity = routeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Route not found with id: " + id));
        existingEntity.setName(route.name());
        var savedEntity = routeRepository.save(existingEntity);
        return toDomainRoute(savedEntity);
    }

    @Transactional
    public void deleteRoute(Integer id) {
        routeRepository.deleteById(id);
    }

    private Route toDomainRoute(RouteEntity entity) {
        return new Route(entity.getId(), entity.getName());
    }
}