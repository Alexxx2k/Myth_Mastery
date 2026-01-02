package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "\"buy\"")
public class BuyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "buy_step_id")
    private Long buyStepId;

    @Column(name = "description", length = 500)
    private String description;

    public BuyEntity() {}

    public BuyEntity(Long id, Long customerId, Long buyStepId, String description) {
        this.id = id;
        this.customerId = customerId;
        this.buyStepId = buyStepId;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public Long getBuyStepId() { return buyStepId; }
    public void setBuyStepId(Long buyStepId) { this.buyStepId = buyStepId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
