package com.alexxx2k.springproject.domain.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BuyProductTest {

    @Test
    void buyProductRecord_ShouldCreateWithAllFields() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order Description", 3L,
                4L, "Product Name", "Category", "Mythology",
                new BigDecimal("10.50"), 2, new BigDecimal("21.00")
        );

        assertEquals(1L, buyProduct.id());
        assertEquals(2L, buyProduct.buyId());
        assertEquals("Order Description", buyProduct.buyDescription());
        assertEquals(3L, buyProduct.buyCustomerId());
        assertEquals(4L, buyProduct.productId());
        assertEquals("Product Name", buyProduct.productName());
        assertEquals("Category", buyProduct.productCategoryName());
        assertEquals("Mythology", buyProduct.productMythologyName());
        assertEquals(new BigDecimal("10.50"), buyProduct.productPrice());
        assertEquals(2, buyProduct.amount());
        assertEquals(new BigDecimal("21.00"), buyProduct.totalPrice());
    }

    @Test
    void buyProductRecord_WithNullAmount_ShouldDefaultToOne() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("10.00"), null, null
        );

        assertEquals(1, buyProduct.amount());
    }

    @Test
    void buyProductRecord_ShouldCalculateTotalPriceWhenNull() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("15.00"), 3, null // totalPrice is null
        );

        assertEquals(new BigDecimal("45.00"), buyProduct.totalPrice()); // 15.00 * 3
    }

    @Test
    void buyProductRecord_ShouldNotRecalculateWhenTotalPriceProvided() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("15.00"), 3, new BigDecimal("40.00") // totalPrice provided
        );
        assertEquals(new BigDecimal("40.00"), buyProduct.totalPrice());
    }

    @Test
    void buyProductRecord_WithDecimalPrice_ShouldCalculateCorrectly() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("7.99"), 5, null
        );

        assertEquals(new BigDecimal("39.95"), buyProduct.totalPrice()); // 7.99 * 5
    }

    @Test
    void buyProductRecord_forCreation_ShouldCreateMinimalObject() {
        BuyProduct buyProduct = BuyProduct.forCreation(10L, 20L, 3);

        assertNull(buyProduct.id());
        assertEquals(10L, buyProduct.buyId());
        assertNull(buyProduct.buyDescription());
        assertNull(buyProduct.buyCustomerId());
        assertEquals(20L, buyProduct.productId());
        assertNull(buyProduct.productName());
        assertNull(buyProduct.productCategoryName());
        assertNull(buyProduct.productMythologyName());
        assertNull(buyProduct.productPrice());
        assertEquals(3, buyProduct.amount());
        assertNull(buyProduct.totalPrice());
    }

    @Test
    void buyProductRecord_forCreation_WithNullAmount_ShouldUseOne() {
        BuyProduct buyProduct = BuyProduct.forCreation(10L, 20L, null);

        assertEquals(1, buyProduct.amount());
    }

    @Test
    void buyProductRecord_Equality_ShouldCompareAllFields() {
        BuyProduct bp1 = new BuyProduct(
                1L, 2L, "Desc", 3L,
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        BuyProduct bp2 = new BuyProduct(
                1L, 2L, "Desc", 3L,
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        assertEquals(bp1, bp2);
    }

    @Test
    void buyProductRecord_Inequality_ShouldDetectDifferences() {
        BuyProduct bp1 = new BuyProduct(
                1L, 2L, "Desc", 3L,
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        BuyProduct bp2 = new BuyProduct(
                2L, 2L, "Desc", 3L, // Different ID
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        assertNotEquals(bp1, bp2);
    }

    @Test
    void buyProductRecord_HashCode_ShouldBeConsistentWithEquals() {
        BuyProduct bp1 = new BuyProduct(
                1L, 2L, "Desc", 3L,
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        BuyProduct bp2 = new BuyProduct(
                1L, 2L, "Desc", 3L,
                4L, "Name", "Cat", "Myth",
                new BigDecimal("10.00"), 2, new BigDecimal("20.00")
        );

        assertEquals(bp1.hashCode(), bp2.hashCode());
    }

    @Test
    void buyProductRecord_ToString_ShouldContainAllFields() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Test Order", 3L,
                4L, "Test Product", "Test Category", "Test Mythology",
                new BigDecimal("99.99"), 5, new BigDecimal("499.95")
        );

        String result = buyProduct.toString();

        assertTrue(result.contains("id=1"));
        assertTrue(result.contains("buyId=2"));
        assertTrue(result.contains("buyDescription=Test Order"));
        assertTrue(result.contains("buyCustomerId=3"));
        assertTrue(result.contains("productId=4"));
        assertTrue(result.contains("productName=Test Product"));
        assertTrue(result.contains("productCategoryName=Test Category"));
        assertTrue(result.contains("productMythologyName=Test Mythology"));
        assertTrue(result.contains("productPrice=99.99"));
        assertTrue(result.contains("amount=5"));
        assertTrue(result.contains("totalPrice=499.95"));
    }

    @Test
    void buyProductRecord_WithNullFieldsInToString_ShouldNotThrow() {
        BuyProduct buyProduct = new BuyProduct(
                null, null, null, null,
                null, null, null, null,
                null, null, null
        );

        assertDoesNotThrow(buyProduct::toString);
        assertNotNull(buyProduct.toString());
    }

    @Test
    void buyProductRecord_ShouldHandleLargeAmounts() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("0.01"), 1000000, null
        );

        assertEquals(new BigDecimal("10000.00"), buyProduct.totalPrice()); // 0.01 * 1,000,000
    }

    @Test
    void buyProductRecord_ShouldHandleNegativeAmount() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                new BigDecimal("10.00"), -5, null
        );

        assertEquals(-5, buyProduct.amount());
        assertEquals(new BigDecimal("-50.00"), buyProduct.totalPrice());
    }

    @Test
    void buyProductRecord_ShouldHandleZeroPrice() {
        BuyProduct buyProduct = new BuyProduct(
                1L, 2L, "Order", 3L,
                4L, "Product", "Cat", "Myth",
                BigDecimal.ZERO, 100, null
        );

        assertEquals(BigDecimal.ZERO, buyProduct.totalPrice());
    }
}
