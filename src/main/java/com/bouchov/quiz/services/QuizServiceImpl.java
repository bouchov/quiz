package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.protocol.ResponseBean;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

@Service
class QuizServiceImpl implements QuizService, DisposableBean, InitializingBean {
    private final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizParticipantRepository quizParticipantRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizResultRepository quizResultRepository;
    private final QuestionRepository questionRepository;
    private final ThreadPoolTaskScheduler quizScheduler;
    private final ScheduledTaskService taskService;
    private final Map<Long, WebSocketSession> sessions;
    private final Map<Long, QuizManager> managers;

    @Autowired
    public QuizServiceImpl(QuizParticipantRepository quizParticipantRepository,
            QuizAnswerRepository quizAnswerRepository,
            QuizResultRepository quizResultRepository,
            QuestionRepository questionRepository,
            ThreadPoolTaskScheduler quizScheduler,
            ScheduledTaskService taskService) {
        this.quizParticipantRepository = quizParticipantRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        this.quizResultRepository = quizResultRepository;
        this.questionRepository = questionRepository;
        this.quizScheduler = quizScheduler;
        this.taskService = taskService;
        sessions = new ConcurrentHashMap<>();
        managers = new ConcurrentHashMap<>();
    }

    @Override
    @Transactional
    public QuizParticipant register(Quiz quiz, User user) {
        if (quiz.getStatus() == QuizStatus.DRAFT) {
            throw new RuntimeException("quiz is not ready");
        }
        if (quiz.getStatus() == QuizStatus.CLOSED) {
            throw new RuntimeException("quiz is closed");
        }
        Page<QuizResult> results = quizResultRepository.findAllByQuizAndStatus(quiz,
                List.of(QuizResultStatus.REGISTER, QuizResultStatus.STARTED),
                PageRequest.of(1, 1, Sort.by(Sort.Direction.ASC, "registrationStarted")));
        QuizResult result;
        if (results.isEmpty()) {
            result = new QuizResult(quiz, QuizResultStatus.REGISTER, new Date());
            result.setParticipants(new ArrayList<>());
            result = quizResultRepository.save(result);
        } else {
            result = results.getContent().get(0);
        }

        if (!managers.containsKey(result.getId())) {
            QuizResult fResult = result;
            managers.computeIfAbsent(result.getId(),
                    (k) -> QuizManagerFactory.getInstance().createManager(this, fResult));
        }
        QuizParticipant participant = quizParticipantRepository.getByQuizResultAndUser(result, user).orElse(null);
        if (participant == null) {
            participant = getManager(result).register(result, user);
            participant = quizParticipantRepository.save(participant);
            result.getParticipants().add(participant);
        }
        return participant;
    }

    private QuizManager getManager(QuizResult result) {
        QuizManager quizManager = managers.get(result.getId());
        if (quizManager == null) {
            throw new RuntimeException("manager not found for " + result);
        }
        return quizManager;
    }

    @Override
    @Transactional
    public void connect(Long participantId, WebSocketSession session) {
        log.debug("register session for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(quizParticipant -> {
            QuizResult result = quizParticipant.getQuizResult();
            if (result.getStatus() == QuizResultStatus.FINISHED) {
                sendMessage(session, new ResponseBean(new QuizResultBean(quizParticipant, result)));
                return;
            }
            sessions.put(participantId, session);
            getManager(result).join(quizParticipant);
        });
    }

    QuizAnswer findActiveAnswer(QuizParticipant quizParticipant) {
        return quizAnswerRepository.findByQuizResultAndAnswererAndStatus(
                quizParticipant.getQuizResult(),
                quizParticipant.getUser(),
                QuizAnswerStatus.ACTIVE).orElse(null);
    }

    void addAnswer(QuizParticipant quizParticipant, Question question) {
        log.debug("select question #{} for {}", question.getId(), quizParticipant.getId());
        User user = quizParticipant.getUser();
        QuizAnswer answer = quizAnswerRepository.save(new QuizAnswer(
                quizParticipant.getQuizResult(),
                question,
                user,
                -1,
                0,
                QuizAnswerStatus.ACTIVE));
        quizParticipant.getAnswers().add(answer);
    }

    void sendMessage(QuizParticipant participant, ResponseBean bean) {
        sendMessage(sessions.get(participant.getId()), bean);
    }

    private void sendMessage(WebSocketSession session, ResponseBean bean) {
        try {
            if (session != null) {
                session.sendMessage(toMessage(bean));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Transactional
    public void answer(Long participantId, int answer) {
        log.debug("answer #{} for {}", answer, participantId);
        quizParticipantRepository.findById(participantId).ifPresent(
                (quizParticipant) -> getManager(quizParticipant.getQuizResult()).answer(quizParticipant, answer));
    }

    @Override
    @Transactional
    public void next(Long participantId) {
        log.debug("next question for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(
                quizParticipant -> getManager(quizParticipant.getQuizResult()).next(quizParticipant));
    }

    private TextMessage toMessage(Object bean)
            throws JsonProcessingException {
        return new TextMessage(new ObjectMapper().writeValueAsString(bean));
    }

    @Override
    public void disconnect(Long participantId) {
        log.debug("unregister session for {}", participantId);
        sessions.remove(participantId);
    }

    public QuizResult getQuizResult(Long resultId) {
        return quizResultRepository.findById(resultId).orElseThrow();
    }

    @Override
    public void afterPropertiesSet()
            throws Exception {
        log.info("INITIALIZED");
        // TODO: 11.12.2020 load quiz and participants
    }

    @Override
    public void destroy()
            throws Exception {
        log.info("DESTROY");
        // TODO: 11.12.2020 stop managers?
    }

    public Future<?> schedule(Runnable task, Instant startTime) {
        return quizScheduler.schedule(() -> taskService.transactional(task), startTime);
    }

    public List<Question> listQuestions(Set<Long> used, int limit) {
        if (used.isEmpty()) {
            return questionRepository.findAll(PageRequest.of(0, limit)).getContent();
        } else {
            return questionRepository.findAllBut(used, PageRequest.of(0, limit));
        }
    }
}
