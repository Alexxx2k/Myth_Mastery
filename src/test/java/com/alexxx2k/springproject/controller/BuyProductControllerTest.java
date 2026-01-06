package com.alexxx2k.springproject.controller;

import com.alexxx2k.springproject.domain.dto.Buy;
import com.alexxx2k.springproject.domain.dto.BuyProduct;
import com.alexxx2k.springproject.domain.dto.Product;
import com.alexxx2k.springproject.service.BuyProductService;
import com.alexxx2k.springproject.service.BuyService;
import com.alexxx2k.springproject.service.CustomerService;
import com.alexxx2k.springproject.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyProductControllerTest {

    @Mock
    private BuyProductService buyProductService;

    @Mock
    private BuyService buyService;

    @Mock
    private ProductService productService;

    @Mock
    private CustomerService customerService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private BuyProductController buyProductController;

    private BuyProduct buyProductDto;
    private Buy buyDto;
    private Product productDto;

    @BeforeEach
    void setUp() {
        buyDto = new Buy(1L, 1L, 1L, "Test Order");

        productDto = new Product(
                1L, 1L, "Potion", 1L, "Greek",
                "Healing Potion", new BigDecimal("10.00"),
                "Restores 50 HP", "healing_potion.jpg"
        );

        buyProductDto = new BuyProduct(
                1L, 1L, "Test Order", 1L,
                1L, "Healing Potion", "Potion", "Greek",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );
    }


    @Test
    void getAllBuyProducts_WithServiceException_ShouldHandleError() {
        when(buyProductService.getAllBuyProducts())
                .thenThrow(new RuntimeException("Database error"));
        when(model.addAttribute("message", "Ошибка: Database error")).thenReturn(model);
        when(model.addAttribute("messageType", "error")).thenReturn(model);

        String viewName = buyProductController.getAllBuyProducts(model);

        assertEquals("mainBuyProduct", viewName);
        verify(model, times(1)).addAttribute("message", "Ошибка: Database error");
        verify(model, times(1)).addAttribute("messageType", "error");
    }


    @Test
    void getBuyProductsByBuy_WithException_ShouldHandleError() {
        when(buyProductService.getBuyProductsByBuyId(1L))
                .thenThrow(new RuntimeException("Error"));
        when(model.addAttribute("message", "Ошибка: Error")).thenReturn(model);
        when(model.addAttribute("messageType", "error")).thenReturn(model);

        String viewName = buyProductController.getBuyProductsByBuy(1L, model);

        assertEquals("buyProductByBuy", viewName);
        verify(model, times(1)).addAttribute("message", "Ошибка: Error");
        verify(model, times(1)).addAttribute("messageType", "error");
    }

    @Test
    void showAddProductForm_ShouldReturnAddProductPage() {
        List<Product> products = Arrays.asList(productDto);

        when(buyService.getBuyById(1L)).thenReturn(Optional.of(buyDto));
        when(productService.getAllProducts()).thenReturn(products);

        when(model.addAttribute("buyId", 1L)).thenReturn(model);
        when(model.addAttribute("buy", buyDto)).thenReturn(model);
        when(model.addAttribute("products", products)).thenReturn(model);

        String viewName = buyProductController.showAddProductForm(1L, model);

        assertEquals("addProductToBuy", viewName);
        verify(buyService, times(1)).getBuyById(1L);
        verify(productService, times(1)).getAllProducts();
    }


    @Test
    void addProductToBuy_WithValidData_ShouldAddProduct() {
        when(buyProductService.addProductToBuy(1L, 1L, 2))
                .thenReturn(buyProductDto);
        when(redirectAttributes.addFlashAttribute("message", "✅ Товар добавлен в заказ!"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "success"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.addProductToBuy(1L, 1L, 2, redirectAttributes);

        assertEquals("redirect:/buy-products/by-buy/1", viewName);
        verify(buyProductService, times(1)).addProductToBuy(1L, 1L, 2);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "✅ Товар добавлен в заказ!");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "success");
    }


    @Test
    void addProductToBuy_WithException_ShouldShowError() {
        when(buyProductService.addProductToBuy(1L, 1L, 2))
                .thenThrow(new IllegalArgumentException("Товар не найден"));
        when(redirectAttributes.addFlashAttribute("message", "❌ Ошибка: Товар не найден"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.addProductToBuy(1L, 1L, 2, redirectAttributes);

        assertEquals("redirect:/buy-products/add/1", viewName);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "❌ Ошибка: Товар не найден");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "error");
    }

    @Test
    void showEditForm_WithExistingId_ShouldReturnEditPage() {
        when(buyProductService.getBuyProductById(1L))
                .thenReturn(Optional.of(buyProductDto));
        when(model.addAttribute("buyProduct", buyProductDto)).thenReturn(model);

        String viewName = buyProductController.showEditForm(1L, model);

        assertEquals("editBuyProduct", viewName);
        verify(buyProductService, times(1)).getBuyProductById(1L);
        verify(model, times(1)).addAttribute("buyProduct", buyProductDto);
    }

    @Test
    void showEditForm_WithNonExistingId_ShouldRedirect() {
        when(buyProductService.getBuyProductById(999L))
                .thenThrow(new IllegalArgumentException("Позиция не найдена"));
        when(model.addAttribute("message", "❌ Ошибка: Позиция не найдена"))
                .thenReturn(model);
        when(model.addAttribute("messageType", "error")).thenReturn(model);

        String viewName = buyProductController.showEditForm(999L, model);

        assertEquals("redirect:/buy-products", viewName);
        verify(model, times(1))
                .addAttribute("message", "❌ Ошибка: Позиция не найдена");
        verify(model, times(1)).addAttribute("messageType", "error");
    }

    @Test
    void updateProductAmount_WithValidAmount_ShouldUpdate() {
        when(buyProductService.updateProductAmount(1L, 5))
                .thenReturn(buyProductDto);
        when(redirectAttributes.addFlashAttribute("message", "✅ Количество обновлено!"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "success"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.updateProductAmount(1L, 5, redirectAttributes);

        assertEquals("redirect:/buy-products/by-buy/1", viewName);
        verify(buyProductService, times(1)).updateProductAmount(1L, 5);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "✅ Количество обновлено!");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "success");
    }

    @Test
    void updateProductAmount_WithException_ShouldShowError() {
        when(buyProductService.updateProductAmount(1L, 0))
                .thenThrow(new IllegalArgumentException("Количество должно быть больше 0"));
        when(redirectAttributes.addFlashAttribute("message", "❌ Ошибка: Количество должно быть больше 0"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.updateProductAmount(1L, 0, redirectAttributes);

        assertEquals("redirect:/buy-products/edit/1", viewName);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "❌ Ошибка: Количество должно быть больше 0");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "error");
    }

    @Test
    void deleteBuyProduct_WithExistingId_ShouldDelete() {
        when(buyProductService.getBuyProductById(1L))
                .thenReturn(Optional.of(buyProductDto));
        doNothing().when(buyProductService).removeProductFromBuy(1L);
        when(redirectAttributes.addFlashAttribute("message", "✅ Товар удален из заказа!"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "success"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.deleteBuyProduct(1L, redirectAttributes);

        assertEquals("redirect:/buy-products/by-buy/1", viewName);
        verify(buyProductService, times(1)).getBuyProductById(1L);
        verify(buyProductService, times(1)).removeProductFromBuy(1L);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "✅ Товар удален из заказа!");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "success");
    }

    @Test
    void deleteBuyProduct_WithDataIntegrityViolation_ShouldShowSpecificError() {
        when(buyProductService.getBuyProductById(1L))
                .thenReturn(Optional.of(buyProductDto));
        doThrow(new DataIntegrityViolationException("foreign key constraint"))
                .when(buyProductService).removeProductFromBuy(1L);
        when(redirectAttributes.addFlashAttribute("message", "❌ Нельзя удалить: существуют связанные объекты"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.deleteBuyProduct(1L, redirectAttributes);

        assertEquals("redirect:/buy-products", viewName);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "❌ Нельзя удалить: существуют связанные объекты");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "error");
    }

    @Test
    void deleteBuyProduct_WithNonExistingId_ShouldShowError() {
        when(buyProductService.getBuyProductById(999L))
                .thenThrow(new IllegalArgumentException("Позиция не найдена"));
        when(redirectAttributes.addFlashAttribute("message", "❌ Ошибка: Позиция не найдена"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.deleteBuyProduct(999L, redirectAttributes);

        assertEquals("redirect:/buy-products", viewName);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "❌ Ошибка: Позиция не найдена");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "error");
    }

    @Test
    void parseProductIds_WithEmptyString_ShouldReturnEmptyList() {
        var result = buyProductController.parseProductIds(null);

        assertTrue(result.isEmpty());
    }

    @Test
    void parseProductIds_WithSingleProduct_ShouldParseCorrectly() {
        var result = buyProductController.parseProductIds("1");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).productId());
        assertEquals(1, result.get(0).amount());
    }

    @Test
    void parseProductIds_WithProductAndAmount_ShouldParseCorrectly() {
        var result = buyProductController.parseProductIds("1:2");

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).productId());
        assertEquals(2, result.get(0).amount());
    }

    @Test
    void parseProductIds_WithMultipleProducts_ShouldParseCorrectly() {
        var result = buyProductController.parseProductIds("1:2, 3:1, 5");

        assertEquals(3, result.size());

        assertEquals(1L, result.get(0).productId());
        assertEquals(2, result.get(0).amount());

        assertEquals(3L, result.get(1).productId());
        assertEquals(1, result.get(1).amount());

        assertEquals(5L, result.get(2).productId());
        assertEquals(1, result.get(2).amount());
    }

    @Test
    void parseProductIds_WithInvalidFormat_ShouldSkipInvalidEntries() {
        var result = buyProductController.parseProductIds("1:2, invalid, 3:abc, 4:5");

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).productId());
        assertEquals(2, result.get(0).amount());
        assertEquals(4L, result.get(1).productId());
        assertEquals(5, result.get(1).amount());
    }

    @Test
    void parseProductIds_WithExtraSpaces_ShouldTrimCorrectly() {
        var result = buyProductController.parseProductIds(" 1 : 2 ,  3 : 1 ");

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).productId());
        assertEquals(2, result.get(0).amount());
        assertEquals(3L, result.get(1).productId());
        assertEquals(1, result.get(1).amount());
    }

    @Test
    void createBuyWithProducts_WithValidData_ShouldCreateBuy() {
        Buy newBuy = new Buy(1L, 1L, 1L, "Test Order");
        when(buyService.createBuy(1L, "Test Order", 1L)).thenReturn(newBuy);
        when(redirectAttributes.addFlashAttribute("message", "✅ Заказ #1 успешно создан!"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "success"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.createBuyWithProducts(1L, "Test Order", redirectAttributes);

        assertEquals("redirect:/buy-products/by-buy/1", viewName);
        verify(buyService, times(1)).createBuy(1L, "Test Order", 1L);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "✅ Заказ #1 успешно создан!");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "success");
    }

    @Test
    void createBuyWithProducts_WithException_ShouldShowError() {
        when(buyService.createBuy(1L, "Test Order", 1L))
                .thenThrow(new RuntimeException("Ошибка создания"));
        when(redirectAttributes.addFlashAttribute("message", "❌ Ошибка создания заказа: Ошибка создания"))
                .thenReturn(redirectAttributes);
        when(redirectAttributes.addFlashAttribute("messageType", "error"))
                .thenReturn(redirectAttributes);

        String viewName = buyProductController.createBuyWithProducts(1L, "Test Order", redirectAttributes);

        assertEquals("redirect:/buy-products/new", viewName);
        verify(redirectAttributes, times(1))
                .addFlashAttribute("message", "❌ Ошибка создания заказа: Ошибка создания");
        verify(redirectAttributes, times(1))
                .addFlashAttribute("messageType", "error");
    }
}
