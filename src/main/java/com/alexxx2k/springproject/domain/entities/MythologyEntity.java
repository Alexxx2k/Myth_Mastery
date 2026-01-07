package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Table(name = "mythology")
@Entity
public class MythologyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    public MythologyEntity() {}

    public MythologyEntity(Long id, String name) {
        this.id = id;
        this.name = name;
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
}
