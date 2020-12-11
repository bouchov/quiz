package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.protocol.CategoryBean;
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
    public List<CategoryBean> showCategory(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) String name) {
        ArrayList<CategoryBean> categories = new ArrayList<>();
        if (id != null) {
            categoryRepository.findById(id).ifPresent((e) -> categories.add(new CategoryBean(e)));
        } else if (name != null) {
            categoryRepository.findByName(name).ifPresent((e) -> categories.add(new CategoryBean(e)));
        } else {
            categoryRepository.findAll().forEach((e) -> categories.add(new CategoryBean(e)));
        }
        return categories;
    }

    @GetMapping(value = "/add")
    public CategoryBean addCategory(@RequestParam String name) {
        Category category = categoryRepository.findByName(name).orElse(null);
        if (category == null) {
            category = new Category();
            category.setName(name);
            categoryRepository.save(category);
        }
        return new CategoryBean(category);
    }
}
