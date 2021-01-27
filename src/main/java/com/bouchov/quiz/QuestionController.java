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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/questions")
class QuestionController extends AbstractController {
    private final HttpSession session;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public QuestionController(HttpSession session,
            CategoryRepository categoryRepository,
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            ObjectMapper objectMapper) {
        this.session = session;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create")
    public QuestionBean add(@RequestBody QuestionBean jsonQuestion)
            throws JsonProcessingException {
        checkAdmin(session);
        Category category = getCategory(jsonQuestion);

        validate(jsonQuestion);

        Question question = questionRepository.save(new Question(
                category,
                jsonQuestion.getText(),
                jsonQuestion.getAnswer(),
                jsonQuestion.getValue(),
                objectMapper.writeValueAsString(jsonQuestion.getOptions())
        ));
        return new QuestionBean(question);
    }

    private void validate(QuestionBean question) {
        if (question.getText() == null || question.getText().isBlank()) {
            throw new InvalidQuestionParameterException("text");
        }
        if (question.getAnswer() == null) {
            throw new InvalidQuestionParameterException("answer");
        }
        if (question.getValue() <= 0) {
            throw new InvalidQuestionParameterException("value");
        }
        int answer = question.getAnswer();
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            throw new InvalidQuestionParameterException("options");
        }
        boolean found = false;
        for (Option option : question.getOptions()) {
            if (answer == option.getId()) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new InvalidQuestionParameterException("answer");
        }
        //normalize
        question.getOptions().sort(Comparator.comparingInt(Option::getId));
        int curId = 0;
        for (Option option : question.getOptions()) {
            if (option.getId() != curId) {
                int old = option.getId();
                option.setId(curId);
                if (answer == old) {
                    answer = curId;
                }
            }
            curId++;
        }
        question.setAnswer(answer);
    }

    private Category getCategory(QuestionBean jsonQuestion) {
        Category category;
        if (jsonQuestion.getCategory() != null) {
            category = categoryRepository.findByName(jsonQuestion.getCategory())
                    .orElseThrow(() -> new CategoryNotFoundException(jsonQuestion.getCategory()));
        } else if (jsonQuestion.getCategoryId() != null) {
            category = categoryRepository.findById(jsonQuestion.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(jsonQuestion.getCategoryId()));
        } else {
            throw new CategoryNotFoundException("null");
        }
        return category;
    }

    @PostMapping("{questionId}/edit")
    public QuestionBean edit(
            @PathVariable Long questionId,
            @RequestBody QuestionBean jsonQuestion)
            throws JsonProcessingException {
        checkAdmin(session);
        Question entity = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        Category category = getCategory(jsonQuestion);

        validate(jsonQuestion);

        entity.setCategory(category);
        entity.setText(jsonQuestion.getText());
        entity.setAnswer(jsonQuestion.getAnswer());
        entity.setValue(jsonQuestion.getValue());
        entity.setOptionsJson(objectMapper.writeValueAsString(jsonQuestion.getOptions()));

        entity = questionRepository.save(entity);
        return new QuestionBean(entity);
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

    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    static class InvalidQuestionParameterException extends RuntimeException {
        public InvalidQuestionParameterException(String message) {
            super(message);
        }
    }
}
