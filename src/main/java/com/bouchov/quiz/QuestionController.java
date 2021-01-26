package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.PageBean;
import com.bouchov.quiz.protocol.QuestionBean;
import com.bouchov.quiz.protocol.QuestionFilterBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/questions")
class QuestionController extends AbstractController {
    private final HttpSession session;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    @Autowired
    public QuestionController(HttpSession session,
            CategoryRepository categoryRepository,
            QuestionRepository questionRepository,
            QuizRepository quizRepository) {
        this.session = session;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    @PostMapping("/add")
    ResponseEntity<?> add(@RequestBody QuestionBean jsonQuestion)
            throws JsonProcessingException {
        checkAdmin(session);
        Category category = categoryRepository.findByName(jsonQuestion.getCategory())
                .orElseThrow(() -> new CategoryNotFoundException(jsonQuestion.getCategory()));
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
    public List<QuestionBean> showQuestions(@RequestParam(required = false) Long categoryId) {
        ArrayList<QuestionBean> questions = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            questionRepository.findAllByCategory(category,
                    Pageable.unpaged()).forEach((e) -> questions.add(new QuestionBean(e)));
        } else {
            questionRepository.findAll().forEach((e) -> questions.add(new QuestionBean(e)));
        }
        return questions;
    }


    @GetMapping("/{questionId}")
    public QuestionBean getQuestion(@PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        return new QuestionBean(question);
    }

    @PostMapping("/list")
    public PageBean<QuestionBean> listQuestions(@RequestBody QuestionFilterBean filter) {
        checkAdmin(session);
        int pageNumber = filter.getPage() == null ? 0 : filter.getPage();
        int pageSize = filter.getSize() == null ? 10 : filter.getSize();
        Sort sort = Sort.by("id");
        Page<Question> page;
        if (filter.getCategoryId() != null) {
            Category category = categoryRepository.findById(filter.getCategoryId()).orElse(null);
            if (category == null) {
                return PageBean.empty();
            }
            page = questionRepository.findAllByCategory(category, PageRequest.of(pageNumber, pageSize, sort));
        } else {
            page = questionRepository.findAll(PageRequest.of(pageNumber, pageSize, sort));
        }
        PageBean<QuestionBean> bean = new PageBean<>(page.getNumber(), page.getSize(), page.getTotalPages());
        bean.setElements(page.map(QuestionBean::new).getContent());
        if (filter.getQuizId() != null) {
            Quiz quiz = quizRepository.findById(filter.getQuizId()).orElse(null);
            if (quiz != null && quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ) {
                Set<Long> questIds = new HashSet<>();
                quiz.getQuestions().forEach(q -> questIds.add(q.getId()));
                bean.getElements().forEach(q -> {
                    if (questIds.contains(q.getId())) {
                        q.setSelected(true);
                    }
                });
            }
        }
        return bean;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class CategoryNotFoundException extends RuntimeException {

        public CategoryNotFoundException(Long categoryId) {
            super("could not find category '" + categoryId + "'.");
        }

        public CategoryNotFoundException(String categoryName) {
            super("could not find category '" + categoryName + "'.");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class QuestionNotFoundException extends RuntimeException {

        public QuestionNotFoundException(Long questionId) {
            super("could not find question '" + questionId + "'.");
        }
    }
}
