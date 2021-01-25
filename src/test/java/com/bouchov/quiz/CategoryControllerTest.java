package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CategoryControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private CategoryRepository categoryRepository;
    private Category testCategory;

    @BeforeEach
    public void setUp() {
        Category category = new Category();
        category.setName(UniqSource.uniqueString("Test category"));
        testCategory = categoryRepository.save(category);
    }
    @AfterEach
    public void tearDown() {
        if (testCategory != null) {
            categoryRepository.delete(testCategory);
            testCategory = null;
        }
    }

    @Test public void getCategoryById() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/adm/category?id=" + testCategory.getId())
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test public void getCategoryByName() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/adm/category?name=" + testCategory.getName())
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
    }

    @Test public void addCategory() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/adm/category/add?name=Long Name")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(MockMvcRequestBuilders.get("/adm/category?name=Long Name")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Long Name"));
    }
}
