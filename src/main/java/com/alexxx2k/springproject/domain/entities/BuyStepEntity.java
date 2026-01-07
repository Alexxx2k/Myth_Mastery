package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

import java.time.LocalDate;

@Table(name = "buy_step")
@Entity
public class BuyStepEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "step_id", nullable = false)
    private Long stepId;

    @Column(name = "date_start")
    private LocalDate dateStart;

    @Column(name = "date_end")
    private LocalDate dateEnd;

    public BuyStepEntity() {}

    public BuyStepEntity(Long id, Long stepId, LocalDate dateStart, LocalDate dateEnd) {
        this.id = id;
        this.stepId = stepId;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long stepId) {
        this.stepId = stepId;
    }

    public LocalDate getDateStart() {
        return dateStart;
    }

    public void setDateStart(LocalDate dateStart) {
        this.dateStart = dateStart;
    }

    public LocalDate getDateEnd() {
        return dateEnd;
    }

    public void setDateEnd(LocalDate dateEnd) {
        this.dateEnd = dateEnd;
    }
}
