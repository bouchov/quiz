package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/quiz")
class QuizController extends AbstractController {
    private static final QuizStatus[] STATUS_FOR_PLAY = new QuizStatus[]{QuizStatus.ACTIVE,QuizStatus.STARTED,QuizStatus.FINISHED};
    private static final QuizStatus[] STATUS_FOR_EDIT = QuizStatus.values();

    private final HttpSession session;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizService quizService;

    @Autowired
    public QuizController(HttpSession session,
                          UserRepository userRepository,
                          QuizRepository quizRepository,
                          QuizService quizService) {
        this.session = session;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.quizService = quizService;
    }

    @RequestMapping("/{quizId}/register")
    public QuizBean startQuiz(@PathVariable Long quizId) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
        QuizBean quizBean = new QuizBean(quiz);
        QuizParticipant participant = quizService.register(quiz, user);
        if (participant != null) {
            quizBean.setResult(new QuizResultBean(participant));
        }
        return quizBean;
    }

    @RequestMapping("/list")
    public List<QuizBean> list(
            @RequestParam(required = false) String name) {
        Optional<User> optional = getUser(session, userRepository);
        QuizStatus[] statuses = STATUS_FOR_PLAY;
        if (optional.isPresent() && optional.get().getRole() == UserRole.ADMIN) {
            statuses = STATUS_FOR_EDIT;
        }
        var list = new ArrayList<QuizBean>();
        if (name == null || name.isEmpty()) {
            quizRepository.findAllByStatus(statuses).forEach(
                    (e) -> list.add(new QuizBean(e)));
        } else {
            quizRepository.findAllByNameAndStatus(name, statuses).forEach(
                    (e) -> list.add(new QuizBean(e)));
        }
        return list;
    }

    @PostMapping("/create")
    public QuizBean createQuiz(@RequestBody QuizBean quiz) {
        checkAdmin(session, userRepository);
        validate(quiz);
        if (quiz.getStatus() == null
                || quiz.getStatus() != QuizStatus.DRAFT
                && quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new InvalidQuizParameterException("status");
        }
        if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ
                && quiz.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizParameterException("status");
        }

        User author = getUser(session, userRepository).orElseThrow();
        Quiz entity = new Quiz();
        entity.setAuthor(author);
        fillParams(quiz, entity);

        try {
            entity = quizRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidQuizParameterException("name");
        }

        return new QuizBean(entity);
    }

    private void fillParams(@RequestBody QuizBean quiz, Quiz entity) {
        entity.setName(quiz.getName());
        entity.setMinPlayers(quiz.getMinPlayers());
        entity.setMaxPlayers(quiz.getMaxPlayers());
        entity.setType(quiz.getType());
        entity.setSelectionStrategy(quiz.getSelectionStrategy());
        entity.setQuestionsNumber(quiz.getQuestionsNumber());
        entity.setStartDate(quiz.getStartDate());
        entity.setStatus(quiz.getStatus());
    }

    @PostMapping("/{quizId}/edit")
    public QuizBean editQuiz(@PathVariable Long quizId,
            @RequestBody QuizBean quiz) {
        checkAdmin(session, userRepository);
        Quiz entity = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
        if (entity.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizParameterException("status");
        }
        validate(quiz);
        if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ
                && quiz.getStatus() == QuizStatus.ACTIVE
                && entity.getQuestions().isEmpty()) {
            throw new InvalidQuizParameterException("status");
        }

        fillParams(quiz, entity);

        try {
            entity = quizRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidQuizParameterException("name");
        }

        return new QuizBean(entity);
    }

    private void validate(QuizBean quiz) {
        if (quiz.getName() == null || quiz.getName().isBlank()) {
            throw new InvalidQuizParameterException("name");
        }
        if (quiz.getMinPlayers() < 1) {
            throw new InvalidQuizParameterException("minPlayers");
        }
        if (quiz.getMaxPlayers() < quiz.getMinPlayers()) {
            throw new InvalidQuizParameterException("maxPlayers");
        }
        if (quiz.getType() != QuizType.SIMPLE) {
            throw new InvalidQuizParameterException("type");
        }
        if (quiz.getSelectionStrategy() != QuestionSelectionStrategy.SOME) {
            if (quiz.getQuestionsNumber() != 0) {
                throw new InvalidQuizParameterException("questionsNumber");
            }
        } else {
            if (quiz.getQuestionsNumber() == 0) {
                throw new InvalidQuizParameterException("questionsNumber");
            }
        }
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    static class QuizNotFoundException extends RuntimeException {
        public QuizNotFoundException(Long id) {
            super("quiz " + id + " not found");
        }
    }

    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    static class InvalidQuizParameterException extends RuntimeException {
        public InvalidQuizParameterException(String message) {
            super(message);
        }
    }
}
