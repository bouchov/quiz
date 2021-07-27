package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.entities.UserRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
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

    @Test public void listCategories() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/categories/list")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test public void addCategory() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/categories/add?name=Long Name")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());

        mvc.perform(MockMvcRequestBuilders.post("/categories/add?name=Long Name")
                .sessionAttr(SessionAttributes.USER_ID, 1L)
                .sessionAttr(SessionAttributes.USER_ROLE, UserRole.ADMIN)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Long Name"));
    }
}
