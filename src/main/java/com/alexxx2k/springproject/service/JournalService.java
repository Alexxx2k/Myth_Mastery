package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.*;
import com.alexxx2k.springproject.domain.entities.AutoEntity;
import com.alexxx2k.springproject.domain.entities.JournalEntity;
import com.alexxx2k.springproject.domain.entities.RouteEntity;
import com.alexxx2k.springproject.repository.AutoRepository;
import com.alexxx2k.springproject.repository.JournalRepository;
import com.alexxx2k.springproject.repository.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class JournalService {

    private final JournalRepository journalRepository;
    private final AutoRepository autoRepository;
    private final RouteRepository routeRepository;

    public JournalService(JournalRepository journalRepository, AutoRepository autoRepository,
                          RouteRepository routeRepository) {
        this.journalRepository = journalRepository;
        this.autoRepository = autoRepository;
        this.routeRepository = routeRepository;
    }

    public List<Journal> getAllJournals() {
        return journalRepository.findAll().stream()
                .map(this::toDomainJournal)
                .toList();
    }

    public Optional<Journal> getJournalById(Integer id) {
        return journalRepository.findById(id)
                .map(this::toDomainJournal);
    }

    public List<Journal> getActiveTrips() {
        return journalRepository.findActiveTrips().stream()
                .map(this::toDomainJournal)
                .toList();
    }

    @Transactional
    public Journal startTrip(Integer autoId, Integer routeId, LocalDateTime customTimeOut) {
        AutoEntity auto = autoRepository.findById(autoId)
                .orElseThrow(() -> new IllegalArgumentException("Auto not found"));
        RouteEntity route = routeRepository.findById(routeId)
                .orElseThrow(() -> new IllegalArgumentException("Route not found"));

        LocalDateTime timeOut;
        if (customTimeOut != null) {
            timeOut = customTimeOut;
        } else {
            timeOut = LocalDateTime.now().withSecond(0).withNano(0);
        }

        var entityToSave = new JournalEntity(null, timeOut, null, auto, route);
        var savedEntity = journalRepository.save(entityToSave);
        return toDomainJournal(savedEntity);
    }

    @Transactional
    public Journal endTrip(Integer journalId, LocalDateTime customTimeIn) {
        JournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("Journal record not found"));

        LocalDateTime timeIn;
        if (customTimeIn != null) {
            timeIn = customTimeIn;
        } else {
            timeIn = LocalDateTime.now().withSecond(0).withNano(0);
        }

        journal.setTimeIn(timeIn);
        var savedEntity = journalRepository.save(journal);
        return toDomainJournal(savedEntity);
    }

    @Transactional
    public Journal updateJournal(Integer journalId, LocalDateTime timeOut, LocalDateTime timeIn, Integer autoId, Integer routeId) {
        JournalEntity journal = journalRepository.findById(journalId)
                .orElseThrow(() -> new IllegalArgumentException("Запись журнала не найдена"));
        if (timeOut != null) journal.setTimeOut(timeOut);
        if (timeIn != null) journal.setTimeIn(timeIn);
        if (autoId != null) {
            AutoEntity auto = autoRepository.findById(autoId)
                    .orElseThrow(() -> new IllegalArgumentException("Автомобиль не найден"));
            journal.setAuto(auto);
        }
        if (routeId != null) {
            RouteEntity route = routeRepository.findById(routeId)
                    .orElseThrow(() -> new IllegalArgumentException("Маршрут не найден"));
            journal.setRoute(route);
        }
        JournalEntity savedEntity = journalRepository.save(journal);
        return toDomainJournal(savedEntity);
    }

    @Transactional
    public void deleteJournal(Integer journalId) {
        journalRepository.deleteById(journalId);
    }

    public Optional<FastestTrip> getFastestTripByRoute(Integer routeId) {
        List<Object[]> results = journalRepository.findFastestTripByRoute(routeId);
        if (results != null && !results.isEmpty()) {
            Object[] row = results.get(0);
            Integer autoId = ((Number) row[0]).intValue();
            String autoNum = (String) row[1];
            String autoMark = (String) row[2];
            String routeName = (String) row[3];
            String durationStr = (String) row[4];
            java.sql.Timestamp timeOutSql = (java.sql.Timestamp) row[5];
            java.sql.Timestamp timeInSql = (java.sql.Timestamp) row[6];
            LocalDateTime timeOut = timeOutSql.toLocalDateTime();
            LocalDateTime timeIn = timeInSql.toLocalDateTime();
            Duration duration = parsePostgresDuration(durationStr);
            FastestTrip trip = new FastestTrip(autoId, autoNum, autoMark, routeName, duration, timeOut, timeIn);
            return Optional.of(trip);
        }
        return Optional.empty();
    }

    public String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;

        if (hours > 0) {
            return String.format("%dч %02dм", hours, minutes);
        } else {
            return String.format("%dм", minutes);
        }
    }

    private Duration parsePostgresDuration(String durationStr) {
        try {
            String[] parts = durationStr.split(":");
            if (parts.length == 3) {
                int hours = Integer.parseInt(parts[0]);
                int minutes = Integer.parseInt(parts[1]);
                return Duration.ofHours(hours).plusMinutes(minutes);
            }
            return Duration.ZERO;
        } catch (Exception e) {
            return Duration.ZERO;
        }
    }

    public ActiveTripsCount getActiveTripsCountByRoute(Integer routeId) {
        Integer count = journalRepository.countActiveTripsByRoute(routeId);
        String routeName = journalRepository.findRouteNameById(routeId).orElse("Маршрут ID: " + routeId);
        return new ActiveTripsCount(routeId, routeName, count != null ? count : 0);
    }

    public List<ActiveTripsCount> getActiveTripsForAllRoutes() {
        List<Route> routes = getAllRoutes();
        return routes.stream()
                .map(route -> getActiveTripsCountByRoute(route.id()))
                .toList();
    }

    private List<Route> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(entity -> new Route(entity.getId(), entity.getName()))
                .toList();
    }

    private Journal toDomainJournal(JournalEntity entity) {
        return new Journal(entity.getId(), entity.getTimeOut(), entity.getTimeIn(),
                entity.getAuto() != null ? entity.getAuto().getId() : null,
                entity.getRoute() != null ? entity.getRoute().getId() : null);
    }
}