package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.HashSet;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alexandre Y. Bouchov
 * Date: 25.01.2021
 * Time: 12:32
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class QuizControllerTest {
    private static final String PWD = "test_pass";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private QuizRepository quizRepository;
    private User admin;
    private Club club;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    public void setUp() {
        User u = new User(
                UniqSource.uniqueString("login"),
                UniqSource.uniqueString("nickname"),
                encoder.encode(PWD),
                UserRole.ADMIN);
        u.setClubs(new HashSet<>());
        admin = userRepository.save(u);
        club = clubRepository.save(new Club(
                UniqSource.uniqueString("club"),
                IdGenerator.generate(),
                admin,
                false));
        admin.getClubs().add(club);
        admin = userRepository.save(admin);
    }

    @Test
    public void testCreateQuiz_SOME_SUCCESS()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.SOME);
        bean.setQuestionsNumber(10);
        bean.setStatus(QuizStatus.DRAFT);

        send("/quiz/create", bean)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testEditQuiz_SOME_SUCCESS()
            throws Exception {
        Quiz entity = new Quiz();
        entity.setAuthor(admin);
        entity.setClub(club);
        entity.setName(UniqSource.uniqueString("quizName"));
        entity.setMinPlayers(1);
        entity.setMaxPlayers(1);
        entity.setType(QuizType.SIMPLE);
        entity.setSelectionStrategy(QuestionSelectionStrategy.SOME);
        entity.setQuestionsNumber(10);
        entity.setStatus(QuizStatus.DRAFT);
        entity = quizRepository.save(entity);

        QuizBean bean = new QuizBean();
        bean.setName(entity.getName());
        bean.setMinPlayers(entity.getMinPlayers());
        bean.setMaxPlayers(entity.getMaxPlayers());
        bean.setType(entity.getType());
        bean.setSelectionStrategy(entity.getSelectionStrategy());
        bean.setQuestionsNumber(entity.getQuestionsNumber());
        bean.setStatus(QuizStatus.ACTIVE);

        send("/quiz/" + entity.getId() + "/edit", bean)
                .andExpect(status().isOk());

        Quiz result = quizRepository.findById(entity.getId()).orElseThrow();
        Assertions.assertEquals(QuizStatus.ACTIVE, result.getStatus());
    }

    @Test
    public void testCreateQuiz_ALL_SUCCESS()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.ALL);
        bean.setQuestionsNumber(0);
        bean.setStatus(QuizStatus.DRAFT);

        send("/quiz/create", bean)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testCreateQuiz_QUIZ_SUCCESS()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.QUIZ);
        bean.setQuestionsNumber(0);
        bean.setStatus(QuizStatus.DRAFT);

        send("/quiz/create", bean)
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void testCreateQuiz_QUIZ_FAIL()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.QUIZ);
        bean.setQuestionsNumber(0);
        bean.setStatus(QuizStatus.ACTIVE);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));
    }

    @Test
    public void testCreateQuiz_SOME_FAIL()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.SOME);
        bean.setQuestionsNumber(0);
        bean.setStatus(QuizStatus.DRAFT);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));

        bean.setQuestionsNumber(3);
        bean.setMinPlayers(0);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));

        bean.setMinPlayers(1);
        bean.setMaxPlayers(0);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));
    }

    @Test
    public void testCreateQuiz_ALL_FAIL()
            throws Exception {
        QuizBean bean = new QuizBean();
        bean.setName(UniqSource.uniqueString("quizName"));
        bean.setMinPlayers(1);
        bean.setMaxPlayers(1);
        bean.setType(QuizType.SIMPLE);
        bean.setSelectionStrategy(QuestionSelectionStrategy.ALL);
        bean.setQuestionsNumber(1);
        bean.setStatus(QuizStatus.DRAFT);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));

        bean.setQuestionsNumber(0);
        bean.setMinPlayers(0);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));

        bean.setMinPlayers(1);
        bean.setMaxPlayers(0);

        send("/quiz/create", bean)
                .andExpect(status().is(HttpStatus.EXPECTATION_FAILED.value()));
    }

    @Test
    @Transactional
    public void testEM() {
        Category entity = new Category(UniqSource.uniqueString("namen"));
        entityManager.persist(entity);
        Assertions.assertNotNull(entity.getId());
    }

    private ResultActions send(String url, QuizBean bean)
            throws Exception {
        MockHttpSession session = login();
        return mvc.perform(MockMvcRequestBuilders.post(url)
                        .session(session)
                        .sessionAttr(SessionAttributes.CLUB_ID, club.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bean))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    public MockHttpSession login() throws Exception {
        return (MockHttpSession) mvc.perform(MockMvcRequestBuilders.post("/")
                        .param("login", admin.getLogin())
                        .param("password", PWD)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession(false);
    }
}
