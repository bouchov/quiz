package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.PageBean;
import com.bouchov.quiz.protocol.QuestionBean;
import com.bouchov.quiz.protocol.QuestionFilterBean;
import com.bouchov.quiz.protocol.UpdateResultBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/questions")
class QuestionController extends AbstractController {
    private final Logger log = LoggerFactory.getLogger(QuestionController.class);

    private final HttpSession session;
    private final CategoryRepository categoryRepository;
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public QuestionController(HttpSession session,
            CategoryRepository categoryRepository,
            QuestionRepository questionRepository,
            QuizRepository quizRepository,
            UserRepository userRepository,
            ClubRepository clubRepository,
            ObjectMapper objectMapper) {
        this.session = session;
        this.categoryRepository = categoryRepository;
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/create")
    public QuestionBean add(
            @RequestBody QuestionBean jsonQuestion)
            throws JsonProcessingException {
        checkAuthorization(session);
        Category category = getCategory(jsonQuestion);

        validate(jsonQuestion);

        User user = getUser(session, userRepository).orElseThrow();
        Club club = getClub(session, clubRepository).orElseThrow(ClubNotFoundException::new);
        if (!user.equals(club.getOwner())) {
            throw new InvalidQuestionParameterException("clubId - user is not owner");
        }
        Question question = questionRepository.save(new Question(
                club,
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
        checkAuthorization(session);
        Question entity = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        Category category = getCategory(jsonQuestion);

        Club club = getClub(session, clubRepository).orElseThrow(ClubNotFoundException::new);
        if (!Objects.equals(club, entity.getClub())) {
            throw new InvalidQuestionParameterException("clubId");
        }
        validate(jsonQuestion);

        User user = getUser(session, userRepository).orElseThrow();
        if (!user.equals(entity.getClub().getOwner())) {
            throw new InvalidQuestionParameterException("clubId - user is not owner");
        }

        entity.setCategory(category);
        entity.setText(jsonQuestion.getText());
        entity.setAnswer(jsonQuestion.getAnswer());
        entity.setValue(jsonQuestion.getValue());
        entity.setOptionsJson(objectMapper.writeValueAsString(jsonQuestion.getOptions()));

        entity = questionRepository.save(entity);
        return new QuestionBean(entity);
    }

    @GetMapping
    public List<QuestionBean> showQuestions(
            @RequestParam(required = false) Long categoryId) {
        checkAuthorization(session);
        Club club = getClub(session, clubRepository).orElseThrow();
        checkOwner(club);
        ArrayList<QuestionBean> questions = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CategoryNotFoundException(categoryId));
            questionRepository.findAllByCategoryAndClub(
                    category,
                    club,
                    Pageable.unpaged()).forEach((e) -> questions.add(new QuestionBean(e)));
        } else {
            questionRepository.findAllByClub(
                    club,
                    Pageable.unpaged()).forEach((e) -> questions.add(new QuestionBean(e)));
        }
        return questions;
    }

    @PostMapping
    public UpdateResultBean addQuestions(
            @RequestBody List<QuestionBean> jsonQuestions)
            throws JsonProcessingException {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Club club = getClub(session, clubRepository).orElseThrow(ClubNotFoundException::new);
        if (!user.equals(club.getOwner())) {
            throw new InvalidQuestionParameterException("clubId - user is not owner");
        }
        int modified = 0, created = 0, total = 0;
        for (QuestionBean jsonQuestion : jsonQuestions) {
            Category category = getCategory(jsonQuestion);

            validate(jsonQuestion);

            if (jsonQuestion.getId() != null) {
                Optional<Question> questionOpt = questionRepository.findById(jsonQuestion.getId());
                if (questionOpt.isPresent()) {
                    log.warn("question " + jsonQuestion.getId() + " already exists");
                    Question question = questionOpt.get();
                    question.setCategory(category);
                    question.setText(jsonQuestion.getText());
                    question.setAnswer(jsonQuestion.getAnswer());
                    question.setOptionsJson(objectMapper.writeValueAsString(jsonQuestion.getOptions()));
                    modified++;
                    questionRepository.save(question);
                    continue;
                }
            }
            created++;
            questionRepository.save(new Question(
                    club,
                    category,
                    jsonQuestion.getText(),
                    jsonQuestion.getAnswer(),
                    jsonQuestion.getValue(),
                    objectMapper.writeValueAsString(jsonQuestion.getOptions())
            ));
        }
        return new UpdateResultBean(created, modified, jsonQuestions.size());
    }


    @GetMapping("/{questionId}")
    public QuestionBean getQuestion(@PathVariable Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
        return new QuestionBean(question);
    }

    @PostMapping("/list")
    public PageBean<QuestionBean> listQuestions(
            @RequestBody QuestionFilterBean filter) {
        checkAuthorization(session);
        Sort sort = Sort.by("id");
        Club club = getClub(session, clubRepository).orElseThrow(ClubNotFoundException::new);
        checkOwner(club);
        Page<Question> page;
        if (filter.getCategoryId() != null) {
            Category category = categoryRepository.findById(filter.getCategoryId()).orElse(null);
            if (category == null) {
                return PageBean.empty();
            }
            page = questionRepository.findAllByCategoryAndClub(category, club, toPageable(filter, sort));
        } else {
            page = questionRepository.findAllByClub(club, toPageable(filter, sort));
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

    private void checkOwner(Club club) {
        User user = getUser(session, userRepository).orElseThrow();
        if (!user.equals(club.getOwner())) {
            throw new ClubNotFoundException("user is not the owner of club " + club.getId());
        }
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
    static class ClubNotFoundException extends RuntimeException {

        public ClubNotFoundException(String message) {
            super(message);
        }

        public ClubNotFoundException() {
            super("club is nor selected");
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
