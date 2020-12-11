package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.Category;

public class CategoryBean {
    private String name;

    public CategoryBean() {
    }

    public CategoryBean(Category category) {
        this.name = category.getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "CategoryBean{" +
                "name='" + name + '\'' +
                '}';
    }
}
