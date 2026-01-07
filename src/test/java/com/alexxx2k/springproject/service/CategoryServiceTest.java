package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Category;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryEntity testEntity;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testEntity = new CategoryEntity(1L, "Test Category", "Medium", "Rare");
        testCategory = new Category(1L, "Test Category", "Medium", "Rare");
    }

    @Test
    void getAllCategories_ShouldReturnCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(testEntity));

        List<Category> categories = categoryService.getAllCategories();

        assertEquals(1, categories.size());
        assertEquals("Test Category", categories.get(0).name());
        assertEquals("Medium", categories.get(0).hazard());
        assertEquals("Rare", categories.get(0).rarity());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void getCategoryById_ShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));

        Optional<Category> result = categoryService.getCategoryById(1L);

        assertTrue(result.isPresent());
        assertEquals("Test Category", result.get().name());
        assertEquals("Medium", result.get().hazard());
        assertEquals("Rare", result.get().rarity());
    }

    @Test
    void getCategoryById_ShouldReturnEmptyWhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Category> result = categoryService.getCategoryById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createCategory_ShouldCreateCategory() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(testEntity);

        Category inputCategory = Category.forCreation("Test Category", "Medium", "Rare");
        Category result = categoryService.createCategory(inputCategory);

        assertNotNull(result);
        assertEquals("Test Category", result.name());
        assertEquals("Medium", result.hazard());
        assertEquals("Rare", result.rarity());
        assertEquals(1L, result.id());
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void createCategory_ShouldThrowWhenCategoryExists() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.createCategory(testCategory);
        });

        assertEquals("Категория с названием 'Test Category' уже существует", exception.getMessage());
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_ShouldUpdateCategory() {
        CategoryEntity updatedEntity = new CategoryEntity(1L, "Updated Category", "High", "Very Rare");
        Category updatedCategory = new Category(1L, "Updated Category", "High", "Very Rare");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(categoryRepository.existsByNameAndIdNot(eq("Updated Category"), eq(1L))).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(updatedEntity);

        Category result = categoryService.updateCategory(1L, updatedCategory);

        assertNotNull(result);
        assertEquals("Updated Category", result.name());
        assertEquals("High", result.hazard());
        assertEquals("Very Rare", result.rarity());
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_ShouldThrowWhenCategoryWithSameNameExists() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(categoryRepository.existsByNameAndIdNot(eq("Test Category"), eq(1L))).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.updateCategory(1L, testCategory);
        });

        assertEquals("Категория с названием 'Test Category' уже существует", exception.getMessage());
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_ShouldUpdateWhenSameNameButSameId() {
        // Тест на случай, когда имя не меняется (та же самая категория)
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testEntity));
        when(categoryRepository.existsByNameAndIdNot(eq("Test Category"), eq(1L))).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(testEntity);

        Category result = categoryService.updateCategory(1L, testCategory);

        assertNotNull(result);
        assertEquals("Test Category", result.name());
        verify(categoryRepository, times(1)).save(any(CategoryEntity.class));
    }

    @Test
    void updateCategory_ShouldThrowWhenNotFound() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.updateCategory(1L, testCategory);
        });

        assertEquals("Категория с ID 1 не найдена", exception.getMessage());
        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void deleteCategory_ShouldDeleteCategory() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        doNothing().when(categoryRepository).deleteById(1L);

        categoryService.deleteCategory(1L);

        verify(categoryRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteCategory_ShouldThrowWhenNotFound() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            categoryService.deleteCategory(1L);
        });

        assertEquals("Категория с ID 1 не найдена", exception.getMessage());
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByName_ShouldReturnTrue() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(true);

        boolean exists = categoryService.existsByName("Test Category");

        assertTrue(exists);
    }

    @Test
    void existsByName_ShouldReturnFalse() {
        when(categoryRepository.existsByName("Test Category")).thenReturn(false);

        boolean exists = categoryService.existsByName("Test Category");

        assertFalse(exists);
    }

    @Test
    void forCreation_ShouldCreateCategoryWithoutId() {
        Category category = Category.forCreation("New Category", "Low", "Common");

        assertNull(category.id());
        assertEquals("New Category", category.name());
        assertEquals("Low", category.hazard());
        assertEquals("Common", category.rarity());
    }
}
