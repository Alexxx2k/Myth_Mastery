package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

    List<ProductEntity> findByCategoryId(Long categoryId);

    List<ProductEntity> findByMythologyId(Long mythologyId);

    boolean existsByName(String name);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p WHERE p.name = :name AND p.id != :id")
    boolean existsByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.mythology WHERE p.id = :id")
    Optional<ProductEntity> findByIdWithAssociations(@Param("id") Long id);

    @Query("SELECT p FROM ProductEntity p LEFT JOIN FETCH p.category LEFT JOIN FETCH p.mythology")
    List<ProductEntity> findAllWithAssociations();
}