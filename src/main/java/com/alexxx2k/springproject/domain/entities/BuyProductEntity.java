package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "buy_product")
public class BuyProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buy_id", nullable = false)
    private BuyEntity buy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private ProductEntity product;

    @Column(name = "amount", nullable = false)
    private Integer amount = 1;

    public BuyProductEntity() {}

    public BuyProductEntity(Long id, BuyEntity buy, ProductEntity product, Integer amount) {
        this.id = id;
        this.buy = buy;
        this.product = product;
        this.amount = amount != null ? amount : 1;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BuyEntity getBuy() {
        return buy;
    }

    public void setBuy(BuyEntity buy) {
        this.buy = buy;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(ProductEntity product) {
        this.product = product;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount != null ? amount : 1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BuyProductEntity that = (BuyProductEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}