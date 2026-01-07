package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import com.alexxx2k.springproject.repository.MythologyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final MythologyRepository mythologyRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          MythologyRepository mythologyRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.mythologyRepository = mythologyRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAllWithAssociations().stream()
                .map(this::toDomainProduct)
                .toList();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findByIdWithAssociations(id)
                .map(this::toDomainProduct);
    }

    public List<Product> getProductsByCategory(Long categoryId) {
        return productRepository.findByCategoryId(categoryId).stream()
                .map(this::toDomainProduct)
                .toList();
    }

    public List<Product> getProductsByMythology(Long mythologyId) {
        return productRepository.findByMythologyId(mythologyId).stream()
                .map(this::toDomainProduct)
                .toList();
    }

    @Transactional
    public Product createProduct(Product product) {
        if (product.name() == null || product.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Название продукта не может быть пустым");
        }

        if (product.categoryId() == null) {
            throw new IllegalArgumentException("Категория не указана");
        }

        if (product.mythologyId() == null) {
            throw new IllegalArgumentException("Мифология не указана");
        }

        if (productRepository.existsByName(product.name().trim())) {
            throw new IllegalArgumentException("Продукт с названием '" + product.name() + "' уже существует");
        }

        CategoryEntity category = categoryRepository.findById(product.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + product.categoryId() + " не найдена"));

        MythologyEntity mythology = mythologyRepository.findById(product.mythologyId())
                .orElseThrow(() -> new IllegalArgumentException("Мифология с ID " + product.mythologyId() + " не найдена"));

        ProductEntity entity = new ProductEntity(
                null,
                category,
                mythology,
                product.name().trim(),
                product.price(),
                product.description() != null ? product.description() : "",
                product.pic() != null ? product.pic() : ""
        );

        ProductEntity savedEntity = productRepository.save(entity);
        return toDomainProduct(savedEntity);
    }

    @Transactional
    public Product updateProduct(Long id, Product product) {
        ProductEntity existingEntity = productRepository.findByIdWithAssociations(id)
                .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + id + " не найдена"));

        if (productRepository.existsByNameAndIdNot(product.name(), id)) {
            throw new IllegalArgumentException("Продукт с названием '" + product.name() + "' уже существует");
        }

        CategoryEntity category = categoryRepository.findById(product.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + product.categoryId() + " не найдена"));

        MythologyEntity mythology = mythologyRepository.findById(product.mythologyId())
                .orElseThrow(() -> new IllegalArgumentException("Мифология с ID " + product.mythologyId() + " не найдена"));

        existingEntity.setCategory(category);
        existingEntity.setMythology(mythology);
        existingEntity.setName(product.name());
        existingEntity.setPrice(product.price());
        existingEntity.setDescription(product.description());
        existingEntity.setPic(product.pic());

        ProductEntity savedEntity = productRepository.save(existingEntity);
        return toDomainProduct(savedEntity);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("Продукт с ID " + id + " не найдена");
        }
        productRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    private Product toDomainProduct(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getCategory() != null ? entity.getCategory().getId() : null,
                entity.getCategory() != null ? entity.getCategory().getName() : null,
                entity.getMythology() != null ? entity.getMythology().getId() : null,
                entity.getMythology() != null ? entity.getMythology().getName() : null,
                entity.getName(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getPic()
        );
    }
}