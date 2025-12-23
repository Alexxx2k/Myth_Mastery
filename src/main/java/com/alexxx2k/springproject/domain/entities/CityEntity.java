package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Table(name = "city")
@Entity
public class CityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "delivery_time", nullable = false)
    private Long deliveryTime;

    public CityEntity() {}

    public CityEntity(Long id, String name, Long deliveryTime) {
        this.id = id;
        this.name = name;
        this.deliveryTime = deliveryTime;
    }

    // Getters, setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(Long deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
}