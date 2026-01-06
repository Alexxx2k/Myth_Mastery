package com.alexxx2k.springproject.domain.dto;

import java.math.BigDecimal;

public record BuyProduct(
        Long id,
        Long buyId,
        String buyDescription,
        Long buyCustomerId,
        Long productId,
        String productName,
        String productCategoryName,
        String productMythologyName,
        BigDecimal productPrice,
        Integer amount,
        BigDecimal totalPrice
) {
    public BuyProduct {
        if (amount == null) amount = 1;
        if (totalPrice == null && productPrice != null) {
            totalPrice = productPrice.multiply(BigDecimal.valueOf(amount));
        }
    }

    public static BuyProduct forCreation(Long buyId, Long productId, Integer amount) {
        return new BuyProduct(
                null,
                buyId,
                null,
                null,
                productId,
                null,
                null,
                null,
                null,
                amount,
                null
        );
    }
}