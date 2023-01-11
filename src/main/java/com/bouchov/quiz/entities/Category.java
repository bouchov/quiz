package com.bouchov.quiz.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Category extends BasicEntity {
    @Column(unique = true, nullable = false)
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

    @Override
    public String toString() {
        return "[Category" +
                " name='" + name + '\'' +
                ']';
    }
}
