package com.bouchov.quiz.entities;

import javax.persistence.*;

@Entity
public class Category extends BasicEntity {
    @Column(unique = true)
    private String name;

    public Category() {
    }

    public Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
