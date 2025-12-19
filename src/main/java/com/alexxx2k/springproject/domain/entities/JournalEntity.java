package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Table(name = "journal")
@Entity
public class JournalEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "time_out")
    private LocalDateTime timeOut;

    @Column(name = "time_in")
    private LocalDateTime timeIn;

    @ManyToOne
    @JoinColumn(name = "auto_id")
    private AutoEntity auto;

    @ManyToOne
    @JoinColumn(name = "route_id")
    private RouteEntity route;

    public JournalEntity() {}

    public JournalEntity(Integer id, LocalDateTime timeOut, LocalDateTime timeIn, AutoEntity auto, RouteEntity route) {
        this.id = id;
        this.timeOut = timeOut;
        this.timeIn = timeIn;
        this.auto = auto;
        this.route = route;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public LocalDateTime getTimeOut() { return timeOut; }
    public void setTimeOut(LocalDateTime timeOut) { this.timeOut = timeOut; }

    public LocalDateTime getTimeIn() { return timeIn; }
    public void setTimeIn(LocalDateTime timeIn) { this.timeIn = timeIn; }

    public AutoEntity getAuto() { return auto; }
    public void setAuto(AutoEntity auto) { this.auto = auto; }

    public RouteEntity getRoute() { return route; }
    public void setRoute(RouteEntity route) { this.route = route; }
}