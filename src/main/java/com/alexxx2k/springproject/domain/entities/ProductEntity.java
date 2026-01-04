package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Table(name = "product")
@Entity
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mythology_id", nullable = false)
    private MythologyEntity mythology;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "price", precision = 8, scale = 2)
    private BigDecimal price;

    @Column(name = "description", length = 100)
    private String description;

    @Column(name = "pic", length = 255) // Храним ключ S3 вместо URL
    private String imageKey;

    public ProductEntity() {}

    public ProductEntity(Long id, CategoryEntity category, MythologyEntity mythology,
                         String name, BigDecimal price, String description, String imageKey) {
        this.id = id;
        this.category = category;
        this.mythology = mythology;
        this.name = name;
        this.price = price;
        this.description = description;
        this.imageKey = imageKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public MythologyEntity getMythology() {
        return mythology;
    }

    public void setMythology(MythologyEntity mythology) {
        this.mythology = mythology;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageKey() {
        return imageKey;
    }

    public void setImageKey(String imageKey) {
        this.imageKey = imageKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductEntity that = (ProductEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "ProductEntity{" +
                "id=" + id +
                ", category=" + (category != null ? category.getId() : "null") +
                ", mythology=" + (mythology != null ? mythology.getId() : "null") +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", imageKey='" + imageKey + '\'' +
                '}';
    }
}
