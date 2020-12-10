package com.bouchov.quiz;

import com.bouchov.quiz.entities.Category;
import com.bouchov.quiz.entities.CategoryRepository;
import com.bouchov.quiz.entities.Question;
import com.bouchov.quiz.entities.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;

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
            @RequestBody Question jsonQuestion
    ) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));
        // TODO: 02.12.2020 check options number and answer
        Question question = questionRepository.save(new Question(
                category,
                null,
                jsonQuestion.getText(),
                jsonQuestion.getAnswer(),
                jsonQuestion.getValue(),
                jsonQuestion.getOptions()
        ));
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(question.getId()).toUri());
        return new ResponseEntity<>(null, httpHeaders, HttpStatus.CREATED);
    }

    @GetMapping
    public List<Question> showQuestions(
            @PathVariable(required = false) Long categoryId) {
        ArrayList<Question> questions = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            return questionRepository.findAllByCategory(category);
        } else {
            questionRepository.findAll().forEach(questions::add);
        }
        return questions;
    }


    @GetMapping("/{questionId}")
    public Question getQuestion(
            @PathVariable(required = false) Long questionId) {
        return questionRepository.findQuestionById(questionId).orElse(null);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class CategoryNotFoundException extends RuntimeException {

        public CategoryNotFoundException(Long categoryId) {
            super("could not find category '" + categoryId + "'.");
        }
    }
}
