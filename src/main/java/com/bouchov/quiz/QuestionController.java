package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.entities.Question;
import com.bouchov.quiz.entities.QuestionRepository;
import com.bouchov.quiz.protocol.QuestionBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/adm/{categoryId}/questions")
class QuestionController {
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;

    @Autowired
    public QuestionController(CategoryRepository categoryRepository,
            QuestionRepository questionRepository) {
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
    }

    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE,
            MediaType.APPLICATION_XML_VALUE})
    ResponseEntity<?> add(
            @PathVariable Long categoryId,
            @RequestBody QuestionBean jsonQuestion
    ) throws JsonProcessingException {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        // TODO: 02.12.2020 check options number and answer
        Question question = questionRepository.save(new Question(
                category,
                jsonQuestion.getText(),
                jsonQuestion.getAnswer(),
                jsonQuestion.getValue(),
                new ObjectMapper().writeValueAsString(jsonQuestion.getOptions())
        ));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(question.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @GetMapping
    public List<QuestionBean> showQuestions(
            @PathVariable(required = false) Long categoryId) {
        ArrayList<QuestionBean> questions = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            questionRepository.findAllByCategory(category).forEach((e) -> questions.add(new QuestionBean(e)));
        } else {
            questionRepository.findAll().forEach((e) -> questions.add(new QuestionBean(e)));
        }
        return questions;
    }


    @GetMapping("/{questionId}")
    public QuestionBean getQuestion(
            @PathVariable Long categoryId,
            @PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        if (!Objects.equals(question.getCategory().getId(), categoryId)) {
            throw new QuestionNotFoundException(questionId);
        }
        return new QuestionBean(question);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class CategoryNotFoundException extends RuntimeException {

        public CategoryNotFoundException(Long categoryId) {
            super("could not find category '" + categoryId + "'.");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class QuestionNotFoundException extends RuntimeException {

        public QuestionNotFoundException(Long questionId) {
            super("could not find question '" + questionId + "'.");
        }
    }
}
