package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Table(name = "routes")
@Entity
public class RouteEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    public RouteEntity() {}

    public RouteEntity(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}