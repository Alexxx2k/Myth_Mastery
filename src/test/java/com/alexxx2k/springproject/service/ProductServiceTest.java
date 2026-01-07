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

    private CategoryEntity testCategory;
    private MythologyEntity testMythology;
    private ProductEntity testProductEntity;
    private Product newProductDto;
    private Product updatedProductDto;

    @BeforeEach
    void setUp() {
        testCategory = new CategoryEntity(1L, "Category", "High", "Common");
        testMythology = new MythologyEntity(1L, "Mythology");
        testProductEntity = new ProductEntity(
                1L, testCategory, testMythology, "Product",
                BigDecimal.TEN, "Description", "pic.jpg"
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
        when(productRepository.findAllWithAssociations()).thenReturn(List.of(testProductEntity));

        List<Product> result = productService.getAllProducts();

        assertEquals(1, result.size());
        verify(productRepository).findAllWithAssociations();
    }

    @Test
    void getProductById_Exists() {
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(testProductEntity));

        Optional<Product> result = productService.getProductById(1L);

        assertTrue(result.isPresent());
        verify(productRepository).findByIdWithAssociations(1L);
    }

    @Test
    void getProductById_NotExists() {
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(1L);

        assertFalse(result.isPresent());
        verify(productRepository).findByIdWithAssociations(1L);
    }

    @Test
    void getProductsByCategory_Success() {
        when(productRepository.findByCategoryId(1L)).thenReturn(List.of(testProductEntity));

        List<Product> result = productService.getProductsByCategory(1L);

        assertEquals(1, result.size());
        verify(productRepository).findByCategoryId(1L);
    }

    @Test
    void getProductsByMythology_Success() {
        when(productRepository.findByMythologyId(1L)).thenReturn(List.of(testProductEntity));

        List<Product> result = productService.getProductsByMythology(1L);

        assertEquals(1, result.size());
        verify(productRepository).findByMythologyId(1L);
    }

    @Test
    void createProduct_Success() {
        ProductEntity savedEntity = new ProductEntity(
                2L, testCategory, testMythology,
                "New Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.existsByName("New Product")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testMythology));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(savedEntity);

        Product result = productService.createProduct(newProductDto);

        assertNotNull(result);
        verify(productRepository).existsByName("New Product");
        verify(categoryRepository).findById(1L);
        verify(mythologyRepository).findById(1L);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void createProduct_EmptyName_ThrowsException() {
        Product invalidProduct = new Product(
                null, 1L, null, 1L, null,
                "", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct)
        );

        assertEquals("Название продукта не может быть пустым", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_NullCategoryId_ThrowsException() {
        Product invalidProduct = new Product(
                null, null, null, 1L, null,
                "Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.createProduct(invalidProduct)
        );

        assertEquals("Категория не указана", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void createProduct_DuplicateName_ThrowsException() {
        when(productRepository.existsByName("Duplicate")).thenReturn(true);

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
        Product productWithInvalidCategory = new Product(
                null, 999L, null, 1L, null,
                "Product", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.existsByName("Product")).thenReturn(false);
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

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

        Product result = productService.createProduct(productWithNullDescription);

        assertEquals("", result.description());
    }

    @Test
    void updateProduct_Success() {
        ProductEntity existingEntity = new ProductEntity(
                1L, testCategory, testMythology,
                "Old", BigDecimal.TEN, "Old Desc", "old.jpg"
        );

        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.existsByNameAndIdNot("Updated", 1L)).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(mythologyRepository.findById(1L)).thenReturn(Optional.of(testMythology));
        when(productRepository.save(any(ProductEntity.class))).thenReturn(existingEntity);

        Product result = productService.updateProduct(1L, updatedProductDto);

        assertNotNull(result);
        verify(productRepository).findByIdWithAssociations(1L);
        verify(productRepository).existsByNameAndIdNot("Updated", 1L);
        verify(productRepository).save(any(ProductEntity.class));
    }

    @Test
    void updateProduct_NotFound_ThrowsException() {
        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.updateProduct(1L, updatedProductDto)
        );

        assertEquals("Продукт с ID 1 не найдена", exception.getMessage());
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_DuplicateName_ThrowsException() {
        ProductEntity existingEntity = new ProductEntity(
                1L, testCategory, testMythology,
                "Old", BigDecimal.TEN, "Desc", "pic.jpg"
        );

        when(productRepository.findByIdWithAssociations(1L)).thenReturn(Optional.of(existingEntity));
        when(productRepository.existsByNameAndIdNot("Duplicate", 1L)).thenReturn(true);

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
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteProduct(1L);

        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_NotFound_ThrowsException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productService.deleteProduct(999L)
        );

        assertEquals("Продукт с ID 999 не найдена", exception.getMessage());
        verify(productRepository, never()).deleteById(anyLong());
    }

    @Test
    void existsByName_ReturnsTrue() {
        when(productRepository.existsByName("Product")).thenReturn(true);

        boolean result = productService.existsByName("Product");

        assertTrue(result);
        verify(productRepository).existsByName("Product");
    }

    @Test
    void existsByName_ReturnsFalse() {
        when(productRepository.existsByName("Unknown")).thenReturn(false);

        boolean result = productService.existsByName("Unknown");

        assertFalse(result);
        verify(productRepository).existsByName("Unknown");
    }
}