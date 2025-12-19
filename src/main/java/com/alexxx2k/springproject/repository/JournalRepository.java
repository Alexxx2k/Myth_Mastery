package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.JournalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface JournalRepository extends JpaRepository<JournalEntity, Integer> {

    @Query("SELECT j FROM JournalEntity j WHERE j.timeIn IS NULL")
    List<JournalEntity> findActiveTrips();

    @Query(value = "SELECT * FROM get_fastest_trip_by_route(:routeId)", nativeQuery = true)
    List<Object[]> findFastestTripByRoute(@Param("routeId") Integer routeId);

    @Query(value = "SELECT get_active_trips_count_by_route(:routeId)", nativeQuery = true)
    Integer countActiveTripsByRoute(@Param("routeId") Integer routeId);

    @Query("SELECT r.name FROM RouteEntity r WHERE r.id = :routeId")
    Optional<String> findRouteNameById(@Param("routeId") Integer routeId);
}