package com.alexxx2k.springproject.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BuyProductEntityTest {

    @Test
    void constructor_ShouldSetFieldsCorrectly() {
        BuyEntity buy = new BuyEntity();
        buy.setId(1L);

        ProductEntity product = new ProductEntity();
        product.setId(2L);

        BuyProductEntity entity = new BuyProductEntity(3L, buy, product, 5);

        assertEquals(3L, entity.getId());
        assertEquals(buy, entity.getBuy());
        assertEquals(product, entity.getProduct());
        assertEquals(5, entity.getAmount());
    }

    @Test
    void constructor_WithNullAmount_ShouldSetDefaultToOne() {
        BuyEntity buy = new BuyEntity();
        ProductEntity product = new ProductEntity();

        BuyProductEntity entity = new BuyProductEntity(1L, buy, product, null);

        assertEquals(1, entity.getAmount());
    }

    @Test
    void defaultConstructor_ShouldCreateEmptyEntity() {
        BuyProductEntity entity = new BuyProductEntity();

        assertNull(entity.getId());
        assertNull(entity.getBuy());
        assertNull(entity.getProduct());
        assertEquals(1, entity.getAmount()); // Default value
    }

    @Test
    void setId_ShouldSetId() {
        BuyProductEntity entity = new BuyProductEntity();

        entity.setId(1L);

        assertEquals(1L, entity.getId());
    }

    @Test
    void setBuy_ShouldSetBuy() {
        BuyProductEntity entity = new BuyProductEntity();
        BuyEntity buy = new BuyEntity();
        buy.setId(1L);

        entity.setBuy(buy);

        assertEquals(buy, entity.getBuy());
        assertEquals(1L, entity.getBuy().getId());
    }

    @Test
    void setProduct_ShouldSetProduct() {
        BuyProductEntity entity = new BuyProductEntity();
        ProductEntity product = new ProductEntity();
        product.setId(2L);
        product.setName("Test Product");

        entity.setProduct(product);

        assertEquals(product, entity.getProduct());
        assertEquals(2L, entity.getProduct().getId());
        assertEquals("Test Product", entity.getProduct().getName());
    }

    @Test
    void setAmount_ShouldSetAmount() {
        BuyProductEntity entity = new BuyProductEntity();

        entity.setAmount(10);

        assertEquals(10, entity.getAmount());
    }

    @Test
    void setAmount_WithNull_ShouldSetDefaultToOne() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setAmount(5); // Set initial value

        entity.setAmount(null);

        assertEquals(1, entity.getAmount());
    }

    @Test
    void setAmount_WithZero_ShouldSetZero() {
        BuyProductEntity entity = new BuyProductEntity();

        entity.setAmount(0);

        assertEquals(0, entity.getAmount());
    }

    @Test
    void setAmount_WithNegative_ShouldSetNegative() {
        BuyProductEntity entity = new BuyProductEntity();

        entity.setAmount(-5);

        assertEquals(-5, entity.getAmount());
    }

    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        BuyProductEntity entity1 = new BuyProductEntity();
        entity1.setId(1L);

        BuyProductEntity entity2 = new BuyProductEntity();
        entity2.setId(1L);

        assertEquals(entity1, entity2);
        assertTrue(entity1.equals(entity2));
    }

    @Test
    void equals_WithDifferentIds_ShouldReturnFalse() {
        BuyProductEntity entity1 = new BuyProductEntity();
        entity1.setId(1L);

        BuyProductEntity entity2 = new BuyProductEntity();
        entity2.setId(2L);

        assertNotEquals(entity1, entity2);
        assertFalse(entity1.equals(entity2));
    }

    @Test
    void equals_WithNullId_ShouldReturnFalse() {
        BuyProductEntity entity1 = new BuyProductEntity();
        entity1.setId(null);

        BuyProductEntity entity2 = new BuyProductEntity();
        entity2.setId(1L);

        assertNotEquals(entity1, entity2);
        assertFalse(entity1.equals(entity2));
    }

    @Test
    void equals_WithNullObject_ShouldReturnFalse() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setId(1L);

        assertFalse(entity.equals(null));
    }

    @Test
    void equals_WithDifferentClass_ShouldReturnFalse() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setId(1L);

        String notAnEntity = "Not an entity";

        assertFalse(entity.equals(notAnEntity));
    }

    @Test
    void equals_WithSameInstance_ShouldReturnTrue() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setId(1L);

        assertTrue(entity.equals(entity));
    }

    @Test
    void hashCode_WithSameId_ShouldBeEqual() {
        BuyProductEntity entity1 = new BuyProductEntity();
        entity1.setId(1L);

        BuyProductEntity entity2 = new BuyProductEntity();
        entity2.setId(1L);

        assertEquals(entity1.hashCode(), entity2.hashCode());
    }

    @Test
    void hashCode_WithNullId_ShouldUseClassHashCode() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setId(null);

        assertEquals(BuyProductEntity.class.hashCode(), entity.hashCode());
    }


    @Test
    void toString_ShouldNotThrowException() {
        BuyProductEntity entity = new BuyProductEntity();
        entity.setId(1L);
        entity.setAmount(5);

        BuyEntity buy = new BuyEntity();
        buy.setId(10L);
        entity.setBuy(buy);

        ProductEntity product = new ProductEntity();
        product.setId(20L);
        entity.setProduct(product);

        assertDoesNotThrow(entity::toString);
        assertNotNull(entity.toString());
    }

    @Test
    void entityRelations_ShouldBeBidirectional() {
        BuyEntity buy = new BuyEntity();
        buy.setId(1L);

        ProductEntity product = new ProductEntity();
        product.setId(2L);

        BuyProductEntity entity = new BuyProductEntity();

        entity.setBuy(buy);
        entity.setProduct(product);
        entity.setAmount(3);

        assertEquals(buy, entity.getBuy());
        assertEquals(product, entity.getProduct());
        assertEquals(3, entity.getAmount());
    }
}
