package com.alexxx2k.springproject.service;

import com.alexxx2k.springproject.domain.dto.BuyProduct;
import com.alexxx2k.springproject.domain.entities.BuyProductEntity;
import com.alexxx2k.springproject.domain.entities.BuyEntity;
import com.alexxx2k.springproject.domain.entities.ProductEntity;
import com.alexxx2k.springproject.domain.entities.CategoryEntity;
import com.alexxx2k.springproject.domain.entities.MythologyEntity;
import com.alexxx2k.springproject.repository.BuyProductRepository;
import com.alexxx2k.springproject.repository.BuyRepository;
import com.alexxx2k.springproject.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuyProductServiceTest {

    @Mock
    private BuyProductRepository buyProductRepository;

    @Mock
    private BuyRepository buyRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BuyProductService buyProductService;

    private BuyEntity buyEntity;
    private ProductEntity productEntity;
    private CategoryEntity categoryEntity;
    private MythologyEntity mythologyEntity;
    private BuyProductEntity buyProductEntity;

    @BeforeEach
    void setUp() {
        // Создаем тестовые данные
        categoryEntity = new CategoryEntity();
        categoryEntity.setId(1L);
        categoryEntity.setName("Potion");

        mythologyEntity = new MythologyEntity();
        mythologyEntity.setId(1L);
        mythologyEntity.setName("Greek");

        buyEntity = new BuyEntity();
        buyEntity.setId(1L);
        buyEntity.setCustomerId(1L);
        buyEntity.setBuyStepId(1L);
        buyEntity.setDescription("Test Order");

        productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setCategory(categoryEntity);
        productEntity.setMythology(mythologyEntity);
        productEntity.setName("Healing Potion");
        productEntity.setPrice(new BigDecimal("10.00"));
        productEntity.setDescription("Restores 50 HP");
        productEntity.setPic("healing_potion.jpg");

        buyProductEntity = new BuyProductEntity();
        buyProductEntity.setId(1L);
        buyProductEntity.setBuy(buyEntity);
        buyProductEntity.setProduct(productEntity);
        buyProductEntity.setAmount(2);
    }

    @Test
    void getAllBuyProducts_ShouldReturnAllBuyProducts() {
        // Arrange
        BuyProductEntity bp2 = new BuyProductEntity();
        bp2.setId(2L);
        bp2.setBuy(buyEntity);
        bp2.setProduct(productEntity);
        bp2.setAmount(3);

        when(buyProductRepository.findAllWithDetails())
                .thenReturn(Arrays.asList(buyProductEntity, bp2));

        // Act
        List<BuyProduct> result = buyProductService.getAllBuyProducts();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals(2L, result.get(1).id());
        verify(buyProductRepository, times(1)).findAllWithDetails();
    }

    @Test
    void getBuyProductsByBuyId_ShouldReturnProductsForSpecificBuy() {
        // Arrange
        when(buyProductRepository.findByBuyIdWithDetails(1L))
                .thenReturn(Arrays.asList(buyProductEntity));

        // Act
        List<BuyProduct> result = buyProductService.getBuyProductsByBuyId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).buyId());
        assertEquals("Healing Potion", result.get(0).productName());
        verify(buyProductRepository, times(1)).findByBuyIdWithDetails(1L);
    }

    @Test
    void getBuyProductById_ShouldReturnBuyProduct() {
        // Arrange
        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(buyProductEntity));

        // Act
        Optional<BuyProduct> result = buyProductService.getBuyProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().id());
        assertEquals(2, result.get().amount());
        verify(buyProductRepository, times(1)).findById(1L);
    }

    @Test
    void getBuyProductById_WithNonExistingId_ShouldReturnEmpty() {
        // Arrange
        when(buyProductRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act
        Optional<BuyProduct> result = buyProductService.getBuyProductById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(buyProductRepository, times(1)).findById(999L);
    }

    @Test
    void addProductToBuy_WithNewProduct_ShouldCreateNewEntry() {
        // Arrange
        when(buyRepository.findById(1L)).thenReturn(Optional.of(buyEntity));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(buyProductRepository.findByBuyIdAndProductId(1L, 1L))
                .thenReturn(Optional.empty());
        when(buyProductRepository.save(any(BuyProductEntity.class)))
                .thenReturn(buyProductEntity);

        // Act
        BuyProduct result = buyProductService.addProductToBuy(1L, 1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.buyId());
        assertEquals(1L, result.productId());
        verify(buyRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(1L);
        verify(buyProductRepository, times(1)).findByBuyIdAndProductId(1L, 1L);
        verify(buyProductRepository, times(1)).save(any(BuyProductEntity.class));
    }

    @Test
    void addProductToBuy_WithExistingProduct_ShouldIncreaseAmount() {
        // Arrange
        BuyProductEntity existingEntity = new BuyProductEntity();
        existingEntity.setId(1L);
        existingEntity.setBuy(buyEntity);
        existingEntity.setProduct(productEntity);
        existingEntity.setAmount(3); // Изначально 3

        when(buyRepository.findById(1L)).thenReturn(Optional.of(buyEntity));
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));
        when(buyProductRepository.findByBuyIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(existingEntity));
        when(buyProductRepository.save(any(BuyProductEntity.class)))
                .thenAnswer(invocation -> {
                    BuyProductEntity saved = invocation.getArgument(0);
                    saved.setAmount(5); // 3 + 2 = 5
                    return saved;
                });

        // Act
        BuyProduct result = buyProductService.addProductToBuy(1L, 1L, 2);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.amount()); // Проверяем увеличенное количество
        verify(buyProductRepository, times(1)).save(existingEntity);
    }

    @Test
    void addProductToBuy_WithNonExistingBuy_ShouldThrowException() {
        // Arrange
        when(buyRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.addProductToBuy(999L, 1L, 2));

        assertEquals("Заказ не найден", exception.getMessage());
        verify(buyRepository, times(1)).findById(999L);
        verify(productRepository, never()).findById(anyLong());
    }

    @Test
    void addProductToBuy_WithNonExistingProduct_ShouldThrowException() {
        // Arrange
        when(buyRepository.findById(1L)).thenReturn(Optional.of(buyEntity));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.addProductToBuy(1L, 999L, 2));

        assertEquals("Товар не найден", exception.getMessage());
        verify(buyRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void updateProductAmount_WithValidAmount_ShouldUpdateSuccessfully() {
        // Arrange
        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(buyProductEntity));
        when(buyProductRepository.save(any(BuyProductEntity.class)))
                .thenReturn(buyProductEntity);

        // Act
        BuyProduct result = buyProductService.updateProductAmount(1L, 5);

        // Assert
        assertNotNull(result);
        assertEquals(5, result.amount());
        verify(buyProductRepository, times(1)).findById(1L);
        verify(buyProductRepository, times(1)).save(buyProductEntity);
    }

    @Test
    void updateProductAmount_WithZeroAmount_ShouldThrowException() {
        // Arrange
        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(buyProductEntity));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.updateProductAmount(1L, 0));

        assertEquals("Количество должно быть больше 0", exception.getMessage());
        verify(buyProductRepository, times(1)).findById(1L);
        verify(buyProductRepository, never()).save(any(BuyProductEntity.class));
    }

    @Test
    void updateProductAmount_WithNegativeAmount_ShouldThrowException() {
        // Arrange
        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(buyProductEntity));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.updateProductAmount(1L, -1));

        assertEquals("Количество должно быть больше 0", exception.getMessage());
        verify(buyProductRepository, times(1)).findById(1L);
        verify(buyProductRepository, never()).save(any(BuyProductEntity.class));
    }

    @Test
    void updateProductAmount_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(buyProductRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.updateProductAmount(999L, 5));

        assertEquals("Позиция не найдена", exception.getMessage());
        verify(buyProductRepository, times(1)).findById(999L);
        verify(buyProductRepository, never()).save(any(BuyProductEntity.class));
    }

    @Test
    void removeProductFromBuy_WithExistingId_ShouldDeleteSuccessfully() {
        // Arrange
        when(buyProductRepository.existsById(1L)).thenReturn(true);
        doNothing().when(buyProductRepository).deleteById(1L);

        // Act
        buyProductService.removeProductFromBuy(1L);

        // Assert
        verify(buyProductRepository, times(1)).existsById(1L);
        verify(buyProductRepository, times(1)).deleteById(1L);
    }

    @Test
    void removeProductFromBuy_WithNonExistingId_ShouldThrowException() {
        // Arrange
        when(buyProductRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> buyProductService.removeProductFromBuy(999L));

        assertEquals("Позиция не найдена", exception.getMessage());
        verify(buyProductRepository, times(1)).existsById(999L);
        verify(buyProductRepository, never()).deleteById(anyLong());
    }

    @Test
    void getTotalPriceByBuyId_ShouldCalculateCorrectTotal() {
        // Arrange
        ProductEntity product2 = new ProductEntity();
        product2.setPrice(new BigDecimal("20.00"));

        BuyProductEntity bp2 = new BuyProductEntity();
        bp2.setProduct(product2);
        bp2.setAmount(3);

        when(buyProductRepository.findByBuyId(1L))
                .thenReturn(Arrays.asList(buyProductEntity, bp2));

        // Act
        BigDecimal total = buyProductService.getTotalPriceByBuyId(1L);

        // Assert
        // 10.00 * 2 = 20.00
        // 20.00 * 3 = 60.00
        // Total = 80.00
        assertEquals(new BigDecimal("80.00"), total);
        verify(buyProductRepository, times(1)).findByBuyId(1L);
    }

    @Test
    void getTotalPriceByBuyId_WithNullProductPrice_ShouldReturnZero() {
        // Arrange
        ProductEntity productWithNullPrice = new ProductEntity();
        productWithNullPrice.setPrice(null);

        BuyProductEntity bp = new BuyProductEntity();
        bp.setProduct(productWithNullPrice);
        bp.setAmount(5);

        when(buyProductRepository.findByBuyId(1L)).thenReturn(Arrays.asList(bp));

        // Act
        BigDecimal total = buyProductService.getTotalPriceByBuyId(1L);

        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getTotalPriceByBuyId_WithNullProduct_ShouldReturnZero() {
        // Arrange
        BuyProductEntity bp = new BuyProductEntity();
        bp.setProduct(null);
        bp.setAmount(5);

        when(buyProductRepository.findByBuyId(1L)).thenReturn(Arrays.asList(bp));

        // Act
        BigDecimal total = buyProductService.getTotalPriceByBuyId(1L);

        // Assert
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void getItemCountByBuyId_ShouldReturnCorrectCount() {
        // Arrange
        BuyProductEntity bp2 = new BuyProductEntity();
        bp2.setId(2L);

        when(buyProductRepository.findByBuyId(1L))
                .thenReturn(Arrays.asList(buyProductEntity, bp2));

        // Act
        int count = buyProductService.getItemCountByBuyId(1L);

        // Assert
        assertEquals(2, count);
        verify(buyProductRepository, times(1)).findByBuyId(1L);
    }

    @Test
    void existsByBuyIdAndProductId_ShouldReturnTrueWhenExists() {
        // Arrange
        when(buyProductRepository.existsByBuyIdAndProductId(1L, 1L))
                .thenReturn(true);

        // Act
        boolean exists = buyProductService.existsByBuyIdAndProductId(1L, 1L);

        // Assert
        assertTrue(exists);
        verify(buyProductRepository, times(1)).existsByBuyIdAndProductId(1L, 1L);
    }

    @Test
    void createBuyWithProducts_ShouldCreateBuyAndProducts() {
        // Arrange
        BuyProductService.CartItem item1 = new BuyProductService.CartItem(1L, 2);
        BuyProductService.CartItem item2 = new BuyProductService.CartItem(2L, 1);
        List<BuyProductService.CartItem> cartItems = Arrays.asList(item1, item2);

        when(buyRepository.save(any(BuyEntity.class))).thenReturn(buyEntity);
        when(productRepository.findById(1L)).thenReturn(Optional.of(productEntity));

        ProductEntity product2 = new ProductEntity();
        product2.setId(2L);
        product2.setPrice(new BigDecimal("15.00"));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));

        when(buyProductRepository.save(any(BuyProductEntity.class)))
                .thenReturn(buyProductEntity);

        // Act
        Long buyId = buyProductService.createBuyWithProducts(1L, "Test Order", cartItems);

        // Assert
        assertNotNull(buyId);
        assertEquals(1L, buyId);
        verify(buyRepository, times(1)).save(any(BuyEntity.class));
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).findById(2L);
        verify(buyProductRepository, times(2)).save(any(BuyProductEntity.class));
    }

    @Test
    void toDomainBuyProduct_ShouldConvertEntityWithAllFields() {
        // Act (используем приватный метод через рефлексию или тестируем через публичный)
        // Для тестирования преобразования, вызовем метод, который использует toDomainBuyProduct
        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(buyProductEntity));

        // Act
        Optional<BuyProduct> result = buyProductService.getBuyProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        BuyProduct dto = result.get();

        assertEquals(1L, dto.id());
        assertEquals(1L, dto.buyId());
        assertEquals("Test Order", dto.buyDescription());
        assertEquals(1L, dto.buyCustomerId());
        assertEquals(1L, dto.productId());
        assertEquals("Healing Potion", dto.productName());
        assertEquals("Potion", dto.productCategoryName());
        assertEquals("Greek", dto.productMythologyName());
        assertEquals(new BigDecimal("10.00"), dto.productPrice());
        assertEquals(2, dto.amount());
        assertEquals(new BigDecimal("20.00"), dto.totalPrice());
    }

    @Test
    void toDomainBuyProduct_WithNullReferences_ShouldHandleGracefully() {
        // Arrange
        BuyProductEntity entityWithNulls = new BuyProductEntity();
        entityWithNulls.setId(1L);
        entityWithNulls.setBuy(null);
        entityWithNulls.setProduct(null);
        entityWithNulls.setAmount(3);

        when(buyProductRepository.findById(1L))
                .thenReturn(Optional.of(entityWithNulls));

        // Act
        Optional<BuyProduct> result = buyProductService.getBuyProductById(1L);

        // Assert
        assertTrue(result.isPresent());
        BuyProduct dto = result.get();

        assertNull(dto.buyId());
        assertNull(dto.buyDescription());
        assertNull(dto.buyCustomerId());
        assertNull(dto.productId());
        assertNull(dto.productName());
        assertNull(dto.productCategoryName());
        assertNull(dto.productMythologyName());
        assertEquals(BigDecimal.ZERO, dto.productPrice());
        assertEquals(3, dto.amount());
        assertEquals(BigDecimal.ZERO, dto.totalPrice());
    }
}