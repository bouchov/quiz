package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.Category;

public class CategoryBean {
    private Long id;
    private String name;

    public CategoryBean() {
    }

    public CategoryBean(Category category) {
        this.id = category.getId();
        this.name = category.getName();
    }

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

    @Override
    public String toString() {
        return "[CategoryBean " +
                "id=" + id +
                ", name='" + name + '\'' +
                ']';
    }
}
