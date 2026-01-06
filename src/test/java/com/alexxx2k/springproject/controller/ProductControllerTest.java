// ProductControllerTest.java (исправленная версия)
package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.service.CategoryService;
import com.alexxx2k.springproject.service.MythologyService;
import com.alexxx2k.springproject.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private MythologyService mythologyService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ProductController productController;

    // Общие тестовые данные
    private Product testProduct;
    private Product createdProduct;
    private Product updatedProduct;
    private List<Product> productList;

    @BeforeEach
    void setUp() {
        // Инициализация общих тестовых данных
        testProduct = new Product(
                1L, 1L, "Category1", 1L, "Mythology1",
                "Test Product", new BigDecimal("99.99"),
                "Test Description", "test.jpg"
        );

        createdProduct = new Product(
                2L, 1L, "Category1", 1L, "Mythology1",
                "New Product", new BigDecimal("100.00"),
                "Description", "pic.jpg"
        );

        updatedProduct = new Product(
                1L, 1L, "Category1", 1L, "Mythology1",
                "Updated Product", new BigDecimal("150.00"),
                "Updated Description", "updated.jpg"
        );

        productList = List.of(testProduct);

        // Настройка security context
        Authentication auth = new TestingAuthenticationToken(
                "user", "password",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // Вспомогательный метод для настройки общих моков
    private void setupCommonMocks() {
        // Возвращаем пустые списки правильных типов
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());
        when(mythologyService.getAllMythologies()).thenReturn(Collections.emptyList());
    }

    @Test
    void getAllProducts_ShouldReturnViewWithProducts() {
        // Arrange
        when(productService.getAllProducts()).thenReturn(productList);
        setupCommonMocks();

        // Act
        String viewName = productController.getAllProducts(model);

        // Assert
        assertEquals("mainProduct", viewName);
        verify(productService).getAllProducts();
        verify(categoryService).getAllCategories();
        verify(mythologyService).getAllMythologies();
        verify(model).addAttribute("productList", productList);
    }

    @Test
    void showCreateProductForm_ShouldReturnCreateView() {
        // Arrange
        setupCommonMocks();

        // Act
        String viewName = productController.showCreateProductForm(model);

        // Assert
        assertEquals("createProduct", viewName);
        verify(model).addAttribute(eq("product"), any(Product.class));
        verify(categoryService).getAllCategories();
        verify(mythologyService).getAllMythologies();
    }

    @Test
    void createProduct_Success_ShouldRedirectWithSuccessMessage() {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // Act
        String redirect = productController.createProduct(
                1L, 1L, "New Product", new BigDecimal("100.00"),
                "Description", "pic.jpg", redirectAttributes
        );

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).createProduct(any(Product.class));
        verifySuccessMessage("Продукт успешно добавлен!");
    }

    @Test
    void createProduct_WithEmptyName_ShouldRedirectWithErrorMessage() {
        // Act
        String redirect = createProductWithInvalidName("");

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService, never()).createProduct(any());
        verifyErrorMessage("Название продукта не может быть пустым");
    }

    @Test
    void createProduct_WithNullName_ShouldRedirectWithErrorMessage() {
        // Act
        String redirect = createProductWithInvalidName(null);

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService, never()).createProduct(any());
        verifyErrorMessage("Название продукта не может быть пустым");
    }

    @Test
    void createProduct_WithWhitespaceName_ShouldRedirectWithErrorMessage() {
        // Act
        String redirect = createProductWithInvalidName("   ");

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService, never()).createProduct(any());
        verifyErrorMessage("Название продукта не может быть пустым");
    }

    @Test
    void createProduct_ServiceThrowsException_ShouldRedirectWithErrorMessage() {
        // Arrange
        when(productService.createProduct(any(Product.class)))
                .thenThrow(new RuntimeException("Service error"));

        // Act
        String redirect = productController.createProduct(
                1L, 1L, "Product", new BigDecimal("100.00"),
                "Description", "pic.jpg", redirectAttributes
        );

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).createProduct(any());
        verifyErrorMessage("Ошибка при добавлении продукта: Service error");
    }

    @Test
    void createProduct_WithNullDescriptionAndPic_ShouldHandleNullValues() {
        // Arrange
        when(productService.createProduct(any(Product.class))).thenReturn(createdProduct);

        // Act
        String redirect = productController.createProduct(
                1L, 1L, "Product", new BigDecimal("100.00"),
                null, null, redirectAttributes
        );

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).createProduct(any(Product.class));
        verifySuccessMessage("Продукт успешно добавлен!");
    }

    @Test
    void showEditProductForm_ProductExists_ShouldReturnEditView() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.of(testProduct));
        setupCommonMocks();

        // Act
        String viewName = productController.showEditProductForm(1L, model);

        // Assert
        assertEquals("editProduct", viewName);
        verify(productService).getProductById(1L);
        verify(model).addAttribute("product", testProduct);
        verify(categoryService).getAllCategories();
        verify(mythologyService).getAllMythologies();
    }

    @Test
    void showEditProductForm_ProductNotFound_ShouldRedirect() {
        // Arrange
        when(productService.getProductById(1L)).thenReturn(Optional.empty());

        // Act
        String viewName = productController.showEditProductForm(1L, model);

        // Assert
        assertEquals("redirect:/products", viewName);
        verify(productService).getProductById(1L);
        verify(model).addAttribute("message", "Ошибка: Продукт с ID 1 не найден");
        verify(model).addAttribute("messageType", "error");
    }

    @Test
    void updateProduct_Success_ShouldRedirectWithSuccessMessage() {
        // Arrange
        when(productService.updateProduct(eq(1L), any(Product.class))).thenReturn(updatedProduct);

        // Act
        String redirect = productController.updateProduct(
                1L, 1L, 1L, "Updated Product", new BigDecimal("150.00"),
                "Updated Description", "updated.jpg", redirectAttributes
        );

        // Assert
        assertEquals("redirect:/products/edit/1", redirect);
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verifySuccessMessage("Продукт успешно обновлен!");
    }

    @Test
    void updateProduct_WithEmptyName_ShouldRedirectWithErrorMessage() {
        // Act
        String redirect = updateProductWithInvalidName("");

        // Assert
        assertEquals("redirect:/products/edit/1", redirect);
        verify(productService, never()).updateProduct(anyLong(), any());
        verifyErrorMessage("Название продукта не может быть пустым");
    }

    @Test
    void updateProduct_ServiceThrowsException_ShouldRedirectWithErrorMessage() {
        // Arrange
        when(productService.updateProduct(eq(1L), any(Product.class)))
                .thenThrow(new RuntimeException("Update error"));

        // Act
        String redirect = productController.updateProduct(
                1L, 1L, 1L, "Product", new BigDecimal("150.00"),
                "Description", "pic.jpg", redirectAttributes
        );

        // Assert
        assertEquals("redirect:/products/edit/1", redirect);
        verify(productService).updateProduct(eq(1L), any(Product.class));
        verifyErrorMessage("Ошибка при обновлении продукта: Update error");
    }

    @Test
    void deleteProduct_Success_ShouldRedirectWithSuccessMessage() {
        // Arrange
        doNothing().when(productService).deleteProduct(1L);

        // Act
        String redirect = productController.deleteProduct(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).deleteProduct(1L);
        verifySuccessMessage("Продукт успешно удален!");
    }

    @Test
    void deleteProduct_DataIntegrityViolation_ShouldRedirectWithSpecificMessage() {
        // Arrange
        doThrow(new DataIntegrityViolationException("Constraint violation"))
                .when(productService).deleteProduct(1L);

        // Act
        String redirect = productController.deleteProduct(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).deleteProduct(1L);
        verify(redirectAttributes).addFlashAttribute(
                "message",
                "Невозможно удалить продукт: Сначала удалите все связанные объекты"
        );
        verify(redirectAttributes).addFlashAttribute("messageType", "error");
    }

    @Test
    void deleteProduct_GeneralException_ShouldRedirectWithErrorMessage() {
        // Arrange
        doThrow(new RuntimeException("General error"))
                .when(productService).deleteProduct(1L);

        // Act
        String redirect = productController.deleteProduct(1L, redirectAttributes);

        // Assert
        assertEquals("redirect:/products", redirect);
        verify(productService).deleteProduct(1L);
        verifyErrorMessage("Ошибка при удалении продукта: General error");
    }

    @Test
    void getProductsByCategory_ShouldReturnViewWithFilteredProducts() {
        // Arrange
        when(productService.getProductsByCategory(1L)).thenReturn(productList);
        setupCommonMocks();

        // Act
        String viewName = productController.getProductsByCategory(1L, model);

        // Assert
        assertEquals("mainProduct", viewName);
        verify(productService).getProductsByCategory(1L);
        verify(categoryService).getAllCategories();
        verify(mythologyService).getAllMythologies();
        verify(model).addAttribute("productList", productList);
    }

    @Test
    void getProductsByMythology_ShouldReturnViewWithFilteredProducts() {
        // Arrange
        when(productService.getProductsByMythology(1L)).thenReturn(productList);
        setupCommonMocks();

        // Act
        String viewName = productController.getProductsByMythology(1L, model);

        // Assert
        assertEquals("mainProduct", viewName);
        verify(productService).getProductsByMythology(1L);
        verify(categoryService).getAllCategories();
        verify(mythologyService).getAllMythologies();
        verify(model).addAttribute("productList", productList);
    }

    // Вспомогательные методы для уменьшения дублирования

    private String createProductWithInvalidName(String name) {
        return productController.createProduct(
                1L, 1L, name, new BigDecimal("100.00"),
                "Description", "pic.jpg", redirectAttributes
        );
    }

    private String updateProductWithInvalidName(String name) {
        return productController.updateProduct(
                1L, 1L, 1L, name, new BigDecimal("150.00"),
                "Description", "pic.jpg", redirectAttributes
        );
    }

    private void verifySuccessMessage(String expectedMessage) {
        verify(redirectAttributes).addFlashAttribute("message", expectedMessage);
        verify(redirectAttributes).addFlashAttribute("messageType", "success");
    }

    private void verifyErrorMessage(String expectedMessage) {
        verify(redirectAttributes).addFlashAttribute(eq("message"), contains(expectedMessage));
        verify(redirectAttributes).addFlashAttribute("messageType", "error");
    }
}