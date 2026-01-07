package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Table(name = "category")
@Entity
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "hazard", length = 50)
    private String hazard;

    @Column(name = "rarity", length = 50)
    private String rarity;

    public CategoryEntity() {}

    public CategoryEntity(Long id, String name, String hazard, String rarity) {
        this.id = id;
        this.name = name;
        this.hazard = hazard;
        this.rarity = rarity;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getHazard() { return hazard; }

    public void setHazard(String hazard) { this.hazard = hazard; }

    public String getRarity() { return rarity; }

    public void setRarity(String rarity) { this.rarity = rarity; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryEntity that = (CategoryEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", hazard='" + hazard + '\'' +
                ", rarity='" + rarity + '\'' +
                '}';
    }
}
