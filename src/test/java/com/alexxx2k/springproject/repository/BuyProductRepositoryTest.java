package com.alexxx2k.springproject.repository;

import com.alexxx2k.springproject.domain.entities.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BuyProductRepositoryTest {

    @Autowired
    private BuyProductRepository buyProductRepository;

    @Autowired
    private BuyRepository buyRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MythologyRepository mythologyRepository;

    private BuyEntity createTestBuy() {
        BuyEntity buy = new BuyEntity();
        buy.setCustomerId(1L);
        buy.setBuyStepId(1L);
        buy.setDescription("Test Order");
        return buyRepository.save(buy);
    }

    private ProductEntity createTestProduct() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Test Category");
        category.setHazard("Low");
        category.setRarity("Common");
        CategoryEntity savedCategory = categoryRepository.save(category);

        MythologyEntity mythology = new MythologyEntity();
        mythology.setName("Test Mythology");
        MythologyEntity savedMythology = mythologyRepository.save(mythology);

        ProductEntity product = new ProductEntity();
        product.setCategory(savedCategory);
        product.setMythology(savedMythology);
        product.setName("Test Product");
        product.setPrice(new BigDecimal("10.00"));
        product.setDescription("Test Description");
        product.setPic("test.jpg");

        return productRepository.save(product);
    }

    private BuyProductEntity createTestBuyProduct() {
        BuyEntity buy = createTestBuy();
        ProductEntity product = createTestProduct();

        BuyProductEntity buyProduct = new BuyProductEntity();
        buyProduct.setBuy(buy);
        buyProduct.setProduct(product);
        buyProduct.setAmount(2);

        return buyProductRepository.save(buyProduct);
    }

    @Test
    void save_ShouldPersistBuyProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();

        // Act
        BuyProductEntity saved = buyProductRepository.save(buyProduct);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(buyProduct.getBuy().getId(), saved.getBuy().getId());
        assertEquals(buyProduct.getProduct().getId(), saved.getProduct().getId());
        assertEquals(2, saved.getAmount());
    }

    @Test
    void findById_ShouldReturnBuyProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long id = buyProduct.getId();

        // Act
        Optional<BuyProductEntity> found = buyProductRepository.findById(id);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(id, found.get().getId());
        assertEquals(2, found.get().getAmount());
    }

    @Test
    void findAll_ShouldReturnAllBuyProducts() {
        // Arrange
        createTestBuyProduct();
        createTestBuyProduct(); // Вторая запись

        // Act
        List<BuyProductEntity> all = buyProductRepository.findAll();

        // Assert
        assertEquals(2, all.size());
    }

    @Test
    void findByBuyId_ShouldReturnProductsForSpecificBuy() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long buyId = buyProduct.getBuy().getId();

        // Act
        List<BuyProductEntity> found = buyProductRepository.findByBuyId(buyId);

        // Assert
        assertEquals(1, found.size());
        assertEquals(buyId, found.get(0).getBuy().getId());
    }

    @Test
    void findByBuyId_WithNonExistingBuy_ShouldReturnEmptyList() {
        // Act
        List<BuyProductEntity> found = buyProductRepository.findByBuyId(999L);

        // Assert
        assertTrue(found.isEmpty());
    }

    @Test
    void findByBuyIdAndProductId_ShouldReturnSpecificBuyProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long buyId = buyProduct.getBuy().getId();
        Long productId = buyProduct.getProduct().getId();

        // Act
        Optional<BuyProductEntity> found = buyProductRepository.findByBuyIdAndProductId(buyId, productId);

        // Assert
        assertTrue(found.isPresent());
        assertEquals(buyId, found.get().getBuy().getId());
        assertEquals(productId, found.get().getProduct().getId());
    }

    @Test
    void findByBuyIdAndProductId_WithNonExisting_ShouldReturnEmpty() {
        // Act
        Optional<BuyProductEntity> found = buyProductRepository.findByBuyIdAndProductId(999L, 999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void existsByBuyIdAndProductId_ShouldReturnTrueWhenExists() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long buyId = buyProduct.getBuy().getId();
        Long productId = buyProduct.getProduct().getId();

        // Act
        boolean exists = buyProductRepository.existsByBuyIdAndProductId(buyId, productId);

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByBuyIdAndProductId_ShouldReturnFalseWhenNotExists() {
        // Act
        boolean exists = buyProductRepository.existsByBuyIdAndProductId(999L, 999L);

        // Assert
        assertFalse(exists);
    }

    @Test
    void deleteByBuyId_ShouldRemoveAllProductsForBuy() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long buyId = buyProduct.getBuy().getId();

        // Создаем второй продукт для того же заказа
        ProductEntity product2 = createTestProduct();
        BuyProductEntity buyProduct2 = new BuyProductEntity();
        buyProduct2.setBuy(buyProduct.getBuy());
        buyProduct2.setProduct(product2);
        buyProduct2.setAmount(1);
        buyProductRepository.save(buyProduct2);

        assertEquals(2, buyProductRepository.findByBuyId(buyId).size());

        // Act
        buyProductRepository.deleteByBuyId(buyId);

        // Assert
        assertEquals(0, buyProductRepository.findByBuyId(buyId).size());
    }

    @Test
    void deleteByProductId_ShouldRemoveAllBuyProductsWithProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long productId = buyProduct.getProduct().getId();

        // Создаем второй заказ с тем же продуктом
        BuyEntity buy2 = createTestBuy();
        BuyProductEntity buyProduct2 = new BuyProductEntity();
        buyProduct2.setBuy(buy2);
        buyProduct2.setProduct(buyProduct.getProduct());
        buyProduct2.setAmount(3);
        buyProductRepository.save(buyProduct2);

        assertTrue(buyProductRepository.existsByBuyIdAndProductId(
                buyProduct.getBuy().getId(), productId));
        assertTrue(buyProductRepository.existsByBuyIdAndProductId(
                buy2.getId(), productId));

        // Act
        buyProductRepository.deleteByProductId(productId);

        // Assert
        assertFalse(buyProductRepository.existsByBuyIdAndProductId(
                buyProduct.getBuy().getId(), productId));
        assertFalse(buyProductRepository.existsByBuyIdAndProductId(
                buy2.getId(), productId));
    }

    @Test
    void findAllWithDetails_ShouldReturnAllWithAssociations() {
        // Arrange
        createTestBuyProduct();

        // Act
        List<BuyProductEntity> all = buyProductRepository.findAllWithDetails();

        // Assert
        assertEquals(1, all.size());
        BuyProductEntity result = all.get(0);

        // Проверяем, что ассоциации загружены
        assertNotNull(result.getBuy());
        assertNotNull(result.getProduct());
        assertNotNull(result.getProduct().getCategory());
        assertNotNull(result.getProduct().getMythology());
    }

    @Test
    void findByBuyIdWithDetails_ShouldReturnWithAssociations() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long buyId = buyProduct.getBuy().getId();

        // Act
        List<BuyProductEntity> found = buyProductRepository.findByBuyIdWithDetails(buyId);

        // Assert
        assertEquals(1, found.size());
        BuyProductEntity result = found.get(0);

        assertNotNull(result.getBuy());
        assertNotNull(result.getProduct());
        assertNotNull(result.getProduct().getCategory());
        assertNotNull(result.getProduct().getMythology());
    }

    @Test
    void findByIdWithDetails_ShouldReturnWithAssociations() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long id = buyProduct.getId();

        // Act
        Optional<BuyProductEntity> found = buyProductRepository.findByIdWithDetails(id);

        // Assert
        assertTrue(found.isPresent());
        BuyProductEntity result = found.get();

        assertNotNull(result.getBuy());
        assertNotNull(result.getProduct());
        assertNotNull(result.getProduct().getCategory());
        assertNotNull(result.getProduct().getMythology());
    }

    @Test
    void update_ShouldUpdateBuyProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long id = buyProduct.getId();

        // Получаем entity для обновления
        BuyProductEntity toUpdate = buyProductRepository.findById(id).orElseThrow();
        toUpdate.setAmount(5);

        // Act
        BuyProductEntity updated = buyProductRepository.save(toUpdate);

        // Assert
        assertEquals(id, updated.getId());
        assertEquals(5, updated.getAmount());

        // Проверяем в БД
        Optional<BuyProductEntity> retrieved = buyProductRepository.findById(id);
        assertTrue(retrieved.isPresent());
        assertEquals(5, retrieved.get().getAmount());
    }

    @Test
    void delete_ShouldRemoveBuyProduct() {
        // Arrange
        BuyProductEntity buyProduct = createTestBuyProduct();
        Long id = buyProduct.getId();
        assertTrue(buyProductRepository.existsById(id));

        // Act
        buyProductRepository.deleteById(id);

        // Assert
        assertFalse(buyProductRepository.existsById(id));
    }

    @Test
    void findByBuyIdAndProductId_WithMultipleBuys_ShouldReturnCorrectOne() {
        // Arrange
        BuyProductEntity buyProduct1 = createTestBuyProduct();
        Long buyId1 = buyProduct1.getBuy().getId();
        Long productId1 = buyProduct1.getProduct().getId();

        // Создаем второй заказ с другим продуктом
        BuyEntity buy2 = createTestBuy();
        ProductEntity product2 = createTestProduct();
        BuyProductEntity buyProduct2 = new BuyProductEntity();
        buyProduct2.setBuy(buy2);
        buyProduct2.setProduct(product2);
        buyProduct2.setAmount(3);
        buyProductRepository.save(buyProduct2);

        // Act
        Optional<BuyProductEntity> found1 = buyProductRepository
                .findByBuyIdAndProductId(buyId1, productId1);
        Optional<BuyProductEntity> found2 = buyProductRepository
                .findByBuyIdAndProductId(buy2.getId(), product2.getId());

        // Assert
        assertTrue(found1.isPresent());
        assertEquals(buyId1, found1.get().getBuy().getId());
        assertEquals(productId1, found1.get().getProduct().getId());

        assertTrue(found2.isPresent());
        assertEquals(buy2.getId(), found2.get().getBuy().getId());
        assertEquals(product2.getId(), found2.get().getProduct().getId());
    }
}