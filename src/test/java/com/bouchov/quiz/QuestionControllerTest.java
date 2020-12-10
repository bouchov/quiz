package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class QuestionControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private CategoryRepository categoryRepository;
    private Category testCategory;

    @BeforeEach
    public void setUp() {
        Category category = new Category();
        category.setName("Test category " + System.currentTimeMillis());
        testCategory = categoryRepository.save(category);
    }

    @Test public void addQuestion() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post(
                "/adm/"+ testCategory.getId() + "/questions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\": \"question text\", \"answer\": 0, \"options\": [\"first option\", \"second option\", \"third option\"]}")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }
}
