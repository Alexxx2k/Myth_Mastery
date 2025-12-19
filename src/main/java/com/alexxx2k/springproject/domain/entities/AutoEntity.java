package com.alexxx2k.springproject.domain.entities;

import jakarta.persistence.*;

@Table(name = "auto")
@Entity
public class AutoEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "num")
    private String num;

    @Column(name = "color")
    private String color;

    @Column(name = "mark")
    private String mark;

    @ManyToOne
    @JoinColumn(name = "personal_id")
    private PersonalEntity personal;

    public AutoEntity() {}

    public AutoEntity(Integer id, String num, String color, String mark, PersonalEntity personal) {
        this.id = id;
        this.num = num;
        this.color = color;
        this.mark = mark;
        this.personal = personal;
    }

    // Геттеры и сеттеры
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getMark() { return mark; }
    public void setMark(String mark) { this.mark = mark; }

    public PersonalEntity getPersonal() { return personal; }
    public void setPersonal(PersonalEntity personal) { this.personal = personal; }
}