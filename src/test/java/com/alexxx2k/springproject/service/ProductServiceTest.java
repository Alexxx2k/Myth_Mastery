package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.repository.CategoryRepository;
import com.alexxx2k.springproject.repository.MythologyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private MythologyRepository mythologyRepository;

    @InjectMocks
    private ProductService productService;

    // Общие данные для тестов
    private CategoryEntity testCategory;
    private MythologyEntity testMythology;
    private ProductEntity testProductEntity;
    private Product testProduct;
    private Product newProductDto;
    private Product updatedProductDto;

    @BeforeEach
    void setUp() {
        // Инициализируем общие данные один раз для всех тестов
        testCategory = new CategoryEntity(1L, "Category", "High", "Common");
        testMythology = new MythologyEntity(1L, "Mythology");
        testProductEntity = new ProductEntity(
                1L, testCategory, testMythology, "Product",
                BigDecimal.TEN, "Description", "pic.jpg"
        );
        testProduct = new Product(
                1L, 1L, "Category", 1L, "Mythology",
                "Product", BigDecimal.TEN, "Description", "pic.jpg"
        );
        newProductDto = new Product(
                null, 1L, null, 1L, null,
                "New Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );
        updatedProductDto = new Product(
                1L, 1L, null, 1L, null,
                "Updated", new BigDecimal("20"), "New Desc", "new.jpg"
        );
    }

    @Test
    void getAllProducts_Success() {
        // Arrange
        when(productRepository.findAllWithAssociations()).thenReturn(List.of(testProductEntity));

        // Act
        List<Product> result = productService.getAllProducts();

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findAllWithAssociations();
    }

    @Test
    void getProductById_Exists() {
        // Arrange
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(testProductEntity));

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        verify(productRepository).findByIdWithAssociations(1L);
    }

    @Test
    void getProductById_NotExists() {
        // Arrange
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.empty());

        // Act
        Optional<Product> result = productService.getProductById(1L);

        // Assert
        assertFalse(result.isPresent());
        verify(productRepository).findByIdWithAssociations(1L);
    }

    @Test
    void getProductsByCategory_Success() {
        // Arrange
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(testProductEntity));

        // Act
        List<Product> result = productService.getProductsByCategory(1L);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void getProductsByMythology_Success() {
        // Arrange
        when(productRepository.findByMythologyId(1L)).thenReturn(List.of(testProductEntity));

        // Act
        List<Product> result = productService.getProductsByMythology(1L);

        // Assert
        assertEquals(1, result.size());
        verify(productRepository).findByMythologyId(1L);
    }

    @Test
    void createProduct_Success() {
        // Arrange
        ProductEntity savedEntity = new ProductEntity(
                2L, testCategory, testMythology,
                "New Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.existsByName("New Product")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testMythology));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        // Act
        Product result = productService.createProduct(newProductDto);

        // Assert
        assertNotNull(result);
        verify(productRepository).existsByName("New Product");
        verify(categoryRepository).findById(1L);
        verify(mythologyRepository).findById(1L);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createProduct_EmptyName_ThrowsException() {
        // Arrange
        Product invalidProduct = new Product(
                null, 1L, null, 1L, null,
                "", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct)
        );

        assertEquals("Название продукта не может быть пустым", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_NullCategoryId_ThrowsException() {
        // Arrange
        Product invalidProduct = new Product(
                null, null, null, 1L, null,
                "Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct)
        );

        assertEquals("Категория не указана", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_DuplicateName_ThrowsException() {
        // Arrange
        when(productRepository.existsByName("Duplicate")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(
                        new Product(null, 1L, null, 1L, null,
                                "Duplicate", BigDecimal.TEN, "Desc", "pic.jpg")
                )
        );

        assertEquals("Продукт с названием 'Duplicate' уже существует", exception.getMessage());
        verify(productRepository).existsByName("Duplicate");
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_CategoryNotFound_ThrowsException() {
        // Arrange
        Product productWithInvalidCategory = new Product(
                null, 999L, null, 1L, null,
                "Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.existsByName("Product")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(productWithInvalidCategory)
        );

        assertEquals("Категория с ID 999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_NullDescription_SetsEmptyString() {
        // Arrange
        Product productWithNullDescription = new Product(
                null, 1L, null, 1L, null,
                "Product", BigDecimal.TEN, null, "pic.jpg"
        );

        ProductEntity savedEntity = new ProductEntity(
                2L, testCategory, testMythology,
                "Product", BigDecimal.TEN, "", "pic.jpg"
        );

        when(productRepository.existsByName("Product")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testMythology));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        // Act
        Product result = productService.createProduct(productWithNullDescription);

        // Assert
        assertEquals("", result.description());
    }

    @Test
    void updateProduct_Success() {
        // Arrange
        ProductEntity existingEntity = new ProductEntity(
                1L, testCategory, testMythology,
                "Old", BigDecimal.TEN, "Old Desc", "old.jpg"
        );

        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.existsByNameAndIdNot("Updated", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testMythology));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(existingEntity);

        // Act
        Product result = productService.updateProduct(1L, updatedProductDto);

        // Assert
        assertNotNull(result);
        verify(productRepository).findByIdWithAssociations(1L);
        verify(productRepository).existsByNameAndIdNot("Updated", 1L);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(1L, updatedProductDto)
        );

        assertEquals("Продукт с ID 1 не найдена", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_DuplicateName_ThrowsException() {
        // Arrange
        ProductEntity existingEntity = new ProductEntity(
                1L, testCategory, testMythology,
                "Old", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.existsByNameAndIdNot("Duplicate", 1L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(
                        1L,
                        new Product(1L, 1L, null, 1L, null,
                                "Duplicate", BigDecimal.TEN, "Desc", "pic.jpg")
                )
        );

        assertEquals("Продукт с названием 'Duplicate' уже существует", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_Success() {
        // Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        // Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(999L)
        );

        assertEquals("Продукт с ID 999 не найдена", exception.getMessage());
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByName_ReturnsTrue() {
        // Arrange
        when(productRepository.existsByName("Product")).thenReturn(true);

        // Act
        boolean result = productService.existsByName("Product");

        // Assert
        assertTrue(result);
        verify(productRepository).existsByName("Product");
    }

    @Test
    void existsByName_ReturnsFalse() {
        // Arrange
        when(productRepository.existsByName("Unknown")).thenReturn(false);

        // Act
        boolean result = productService.existsByName("Unknown");

        // Assert
        assertFalse(result);
        verify(productRepository).existsByName("Unknown");
    }
}