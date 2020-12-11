package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.protocol.CategoryBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class CategoryLoader {
    private final String fileName;

    public CategoryLoader(String fileName) {
        this.fileName = fileName;
    }

    public CategoryLoader() {
        this("categories.json");
    }

    public void load(CategoryRepository repository) throws IOException {
        try(InputStream stream = CategoryLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            CategoryBean[] beans = mapper.readValue(stream, CategoryBean[].class);
            for (CategoryBean category : beans) {
                repository.save(new Category(category.getName()));
            }
        }
    }
}
