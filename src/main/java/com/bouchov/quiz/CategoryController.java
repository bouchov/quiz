package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.protocol.CategoryBean;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/categories")
class CategoryController extends AbstractController {
    private final HttpSession session;
    private final CategoryRepository categoryRepository;

    @Autowired
    CategoryController(
            HttpSession session,
            CategoryRepository categoryRepository) {
        this.session = session;
        this.categoryRepository = categoryRepository;
    }

    @PostMapping("/list")
    public List<CategoryBean> list(
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

    @PostMapping("/add")
    public CategoryBean addCategory(@RequestParam String name) {
        checkAdmin(session);
        Category category = categoryRepository.findByName(name).orElse(null);
        if (category == null) {
            category = new Category();
            category.setName(name);
            categoryRepository.save(category);
        }
        return new CategoryBean(category);
    }
}
