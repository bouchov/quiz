package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.*;
import com.bouchov.quiz.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/quiz")
class QuizController extends AbstractController {
    private static final QuizStatus[] STATUS_FOR_PLAY = new QuizStatus[]{QuizStatus.ACTIVE};
    private static final QuizStatus[] STATUS_FOR_EDIT = QuizStatus.values();

    private final HttpSession session;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final ClubRepository clubRepository;
    private final QuestionRepository questionRepository;
    private final QuizService quizService;

    @Autowired
    public QuizController(HttpSession session,
            UserRepository userRepository,
            QuizRepository quizRepository,
            ClubRepository clubRepository,
            QuestionRepository questionRepository,
            QuizService quizService) {
        this.session = session;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.clubRepository = clubRepository;
        this.questionRepository = questionRepository;
        this.quizService = quizService;
    }

    @RequestMapping("/{quizId}/register")
    public QuizBean startQuiz(
            @PathVariable Long quizId) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
        Optional<Club> club = getClub(session, clubRepository);
        if (club.isEmpty() || !Objects.equals(club.get(), quiz.getClub())) {
            throw new QuizNotFoundException(quizId);
        }
        QuizBean quizBean = new QuizBean(quiz);
        QuizParticipant participant = quizService.register(quiz, user);
        if (participant != null) {
            quizBean.setResult(new QuizResultBean(participant, participant.getQuizResult()));
        }
        return quizBean;
    }

    @RequestMapping("/list")
    public PageBean<QuizBean> list(
            @RequestBody QuizFilterBean filter) {
        checkAuthorization(session);
        QuizStatus[] statuses = STATUS_FOR_PLAY;
        User user = getUser(session, userRepository).orElseThrow();
        Club club = getClub(session, clubRepository).orElseThrow(ClubNotFoundException::new);
        if (user.equals(club.getOwner())) {
            statuses = STATUS_FOR_EDIT;
        }
        PageRequest pageable = toPageable(filter, Sort.by("name"));
        Page<Quiz> page;
        if (filter.getName() == null || filter.getName().isEmpty()) {
            page = quizRepository.findAllByStatus(club, pageable, statuses);
        } else {
            page  = quizRepository.findAllByNameUpperAndStatus(club, filter.getName().toUpperCase(Locale.ROOT), pageable, statuses);
        }
        PageBean<QuizBean> bean = new PageBean<>(page.getNumber(), page.getSize(), page.getTotalPages());
        bean.setElements(page.map(QuizBean::new).getContent());
        return bean;
    }

    @PostMapping("/create")
    public QuizBean createQuiz(
            @RequestBody QuizBean quiz) {
        checkAuthorization(session);
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
        Club club = getClub(session, clubRepository).orElseThrow();
        User author = getUser(session, userRepository).orElseThrow();
        Quiz entity = new Quiz();
        entity.setAuthor(author);
        entity.setClub(club);
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
        entity.setStatus(quiz.getStatus());
    }

    @PostMapping("/{quizId}/edit")
    public QuizBean editQuiz(
            @PathVariable Long quizId,
            @RequestBody QuizBean quiz) {
        checkAuthorization(session);
        Quiz entity = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
        if (entity.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizParameterException("status");
        }
        validate(quiz);

        if (quiz.getSelectionStrategy() == QuestionSelectionStrategy.QUIZ
                && quiz.getStatus() == QuizStatus.ACTIVE) {
            if (entity.getQuestions().isEmpty()) {
                throw new InvalidQuizParameterException("status");
            } else {
                quiz.setQuestionsNumber(entity.getQuestions().size());
            }
        }
        Optional<Club> club = getClub(session, clubRepository);
        if (club.isEmpty() || !Objects.equals(club.get(), entity.getClub())) {
            throw new ClubNotFoundException();
        }
        fillParams(quiz, entity);

        try {
            entity = quizRepository.save(entity);
        } catch (DataIntegrityViolationException e) {
            throw new InvalidQuizParameterException("name");
        }

        return new QuizBean(entity);
    }

    @PostMapping("/{quizId}/questions")
    public long[] changeQuestions(
            @PathVariable Long quizId,
            @RequestBody ChangedCollectionBean changes) {
        checkAuthorization(session);
        Quiz entity = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotFoundException(quizId));
        Optional<Club> club = getClub(session, clubRepository);
        if (club.isEmpty() || !Objects.equals(club.get(), entity.getClub())) {
            throw new ClubNotFoundException();
        }
        if (entity.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidQuizParameterException("status");
        }
        if (changes.getAdded() != null && !changes.getAdded().isEmpty()) {
            for (Long id : changes.getAdded()) {
                questionRepository.findById(id).ifPresent((question) -> addQuestion(entity, question));
            }
        }
        if (changes.getRemoved() != null && !changes.getRemoved().isEmpty()) {
            for (Long id : changes.getRemoved()) {
                questionRepository.findById(id).ifPresent((question) -> entity.getQuestions().remove(question));
            }
        }
        quizRepository.save(entity);
        return entity.getQuestions().stream().mapToLong(BasicEntity::getId).toArray();
    }

    private void addQuestion(Quiz entity, Question question) {
        for (Question q : entity.getQuestions()) {
            if (q.getId().equals(question.getId())) {
                return;
            }
        }
        entity.getQuestions().add(question);
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class QuizNotFoundException extends RuntimeException {
        public QuizNotFoundException(Long id) {
            super("quiz " + id + " not found");
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ClubNotFoundException extends RuntimeException {
        public ClubNotFoundException() {
            super("club is not selected");
        }
    }

    @ResponseStatus(HttpStatus.EXPECTATION_FAILED)
    static class InvalidQuizParameterException extends RuntimeException {
        public InvalidQuizParameterException(String message) {
            super(message);
        }
    }
}
