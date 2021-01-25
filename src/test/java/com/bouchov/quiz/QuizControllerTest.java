package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Alexandre Y. Bouchov
 * Date: 25.01.2021
 * Time: 12:32
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
@AutoConfigureMockMvc
public class QuizControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private QuizRepository quizRepository;
    private User admin;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        User u = new User(
                UniqSource.uniqueString("login"),
                UniqSource.uniqueString("nickname"),
                "test",
                UserRole.ADMIN);
        admin = userRepository.save(u);
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

    private ResultActions send(String url, QuizBean bean)
            throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post(url)
                .sessionAttr(SessionAttributes.USER_ID, admin.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bean))
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
    }
}