package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.BuyProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuyProductRepository extends JpaRepository<BuyProductEntity, Long> {

    List<BuyProductEntity> findByBuyId(Long buyId);

    Optional<BuyProductEntity> findByBuyIdAndProductId(Long buyId, Long productId);

    boolean existsByBuyIdAndProductId(Long buyId, Long productId);

    void deleteByBuyId(Long buyId);

    void deleteByProductId(Long productId);

    @Query("SELECT bp FROM BuyProductEntity bp " +
            "LEFT JOIN FETCH bp.buy " +
            "LEFT JOIN FETCH bp.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.mythology " +
            "ORDER BY bp.id DESC")
    List<BuyProductEntity> findAllWithDetails();

    @Query("SELECT bp FROM BuyProductEntity bp " +
            "LEFT JOIN FETCH bp.buy " +
            "LEFT JOIN FETCH bp.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.mythology " +
            "WHERE bp.buy.id = :buyId")
    List<BuyProductEntity> findByBuyIdWithDetails(@Param("buyId") Long buyId);

    @Query("SELECT bp FROM BuyProductEntity bp " +
            "LEFT JOIN FETCH bp.buy " +
            "LEFT JOIN FETCH bp.product p " +
            "LEFT JOIN FETCH p.category " +
            "LEFT JOIN FETCH p.mythology " +
            "WHERE bp.id = :id")
    Optional<BuyProductEntity> findByIdWithDetails(@Param("id") Long id);
}
