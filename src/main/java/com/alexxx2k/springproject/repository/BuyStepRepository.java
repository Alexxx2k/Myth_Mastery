package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.BuyStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BuyStepRepository extends JpaRepository<BuyStepEntity, Long> {

    List<BuyStepEntity> findByStepId(Long stepId);

    boolean existsByStepId(Long stepId);

    @Query("SELECT b FROM BuyStepEntity b WHERE b.dateStart <= :currentDate AND (b.dateEnd IS NULL OR b.dateEnd >= :currentDate)")
    List<BuyStepEntity> findActiveSteps(@Param("currentDate") LocalDate currentDate);

    @Query("SELECT b FROM BuyStepEntity b WHERE b.dateStart BETWEEN :startDate AND :endDate OR b.dateEnd BETWEEN :startDate AND :endDate")
    List<BuyStepEntity> findByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}


