package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Category;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toDomainCategory)
                .toList();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .map(this::toDomainCategory);
    }

    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.name())) {
            throw new IllegalArgumentException("Категория с названием '" + category.name() + "' уже существует");
        }

        CategoryEntity entity = new CategoryEntity(
                null,
                category.name(),
                category.hazard(),
                category.rarity()
        );
        CategoryEntity savedEntity = categoryRepository.save(entity);
        return toDomainCategory(savedEntity);
    }

    @Transactional
    public Category updateCategory(Long id, Category category) {
        CategoryEntity existingEntity = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Категория с ID " + id + " не найдена"));

        if (categoryRepository.existsByNameAndIdNot(category.name(), id)) {
            throw new IllegalArgumentException("Категория с названием '" + category.name() + "' уже существует");
        }

        existingEntity.setName(category.name());
        existingEntity.setHazard(category.hazard());
        existingEntity.setRarity(category.rarity());

        CategoryEntity savedEntity = categoryRepository.save(existingEntity);
        return toDomainCategory(savedEntity);
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Категория с ID " + id + " не найдена");
        }
        categoryRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    private Category toDomainCategory(CategoryEntity entity) {
        return new Category(
                entity.getId(),
                entity.getName(),
                entity.getHazard(),
                entity.getRarity()
        );
    }
}
