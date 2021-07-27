package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class QuestionControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private QuestionRepository questionRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    private Category testCategory;
    private User testUser;
    private Club testClub;

    @BeforeEach
    public void setUp() {
        Category category = new Category();
        category.setName("Test category " + System.currentTimeMillis());
        testCategory = categoryRepository.save(category);
        testUser = userRepository.save(
                new User(
                        UniqSource.uniqueString("login"),
                        UniqSource.uniqueString("nickname"),
                        UniqSource.uniqueString("pass"),
                        UserRole.PLAYER));
        testClub = clubRepository.save(
                new Club(
                        UniqSource.uniqueString("name"),
                        IdGenerator.generate(),
                        testUser,
                        true
                )
        );
    }

    @Test public void addQuestion() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/questions/create")
                .sessionAttr(SessionAttributes.USER_ID, testUser.getId())
                .sessionAttr(SessionAttributes.USER_ROLE, testUser.getRole())
                .sessionAttr(SessionAttributes.CLUB_ID, testClub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\""+ testCategory.getName() + "\",\"text\":\"question text\",\"answer\":0,\"value\":1," +
                        "\"options\":[{\"id\":0,\"name\":\"first option\"},{\"id\":1,\"name\":\"second option\"},{\"id\":2,\"name\":\"third option\"}]}")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test public void addQuestions() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/questions")
                .sessionAttr(SessionAttributes.USER_ID, testUser.getId())
                .sessionAttr(SessionAttributes.USER_ROLE, testUser.getRole())
                .sessionAttr(SessionAttributes.CLUB_ID, testClub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("[{\"category\":\""+ testCategory.getName() + "\",\"text\":\"question text\",\"answer\":0,\"value\":1," +
                        "\"options\":[{\"id\":0,\"name\":\"first option\"},{\"id\":1,\"name\":\"second option\"},{\"id\":2,\"name\":\"third option\"}]}]"))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }

    @Test public void listQuestions() throws Exception {
        questionRepository.save(new Question(
                testClub,
                testCategory,
                "question 1",
                0,
                1,
                objectMapper.writeValueAsString(Arrays.asList(
                        new Option(0, "op0"),
                        new Option(1, "op1"),
                        new Option(2, "op2")))
        ));
        questionRepository.save(new Question(
                testClub,
                testCategory,
                "question 2",
                0,
                1,
                objectMapper.writeValueAsString(Arrays.asList(
                        new Option(0, "op0"),
                        new Option(1, "op1"),
                        new Option(2, "op2")))
        ));
        questionRepository.save(new Question(
                testClub,
                testCategory,
                "question 3",
                0,
                1,
                objectMapper.writeValueAsString(Arrays.asList(
                        new Option(0, "op0"),
                        new Option(1, "op1"),
                        new Option(2, "op2")))
        ));
        questionRepository.save(new Question(
                testClub,
                testCategory,
                "question 4",
                0,
                1,
                objectMapper.writeValueAsString(Arrays.asList(
                        new Option(0, "op0"),
                        new Option(1, "op1"),
                        new Option(2, "op2")))
        ));
        mvc.perform(MockMvcRequestBuilders.post("/questions/list")
                .sessionAttr(SessionAttributes.USER_ID, testUser.getId())
                .sessionAttr(SessionAttributes.USER_ROLE, testUser.getRole())
                .sessionAttr(SessionAttributes.CLUB_ID, testClub.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"categoryId\": \"" + testCategory.getId() + "\", \"page\": 0, \"size\": 3}")
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful());
    }
}
