package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/adm/category")
class CategoryController {
    private final CategoryRepository categoryRepository;

    @Autowired
    CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping
    public List<Category> showCategory(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name) {
        ArrayList<Category> categories = new ArrayList<>();
        if (id != null) {
            categoryRepository.findById(id).ifPresent(categories::add);
        } else if (name != null) {
            categoryRepository.findByName(name).ifPresent(categories::add);
        } else {
            categoryRepository.findAll().forEach(categories::add);
        }
        return categories;
    }

    @GetMapping(value = "/add")
    public Category addCategory(@RequestParam String name) {
        Category category = categoryRepository.findByName(name).orElse(null);
        if (category == null) {
            category = new Category();
            category.setName(name);
            categoryRepository.save(category);
        }
        return category;
    }
}
