package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
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
        log.debug("register user {} for quiz {}", user.getId(), quiz.getId());
        Page<QuizResult> results = quizResultRepository.findAllByQuizAndStatus(quiz,
                List.of(QuizResultStatus.REGISTER, QuizResultStatus.STARTED),
                PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "registrationStarted")));
        QuizResult result;
        if (results.isEmpty()) {
            result = new QuizResult(quiz, QuizResultStatus.REGISTER, new Date());
            result.setParticipants(new ArrayList<>());
            result = quizResultRepository.save(result);
        } else {
            result = results.getContent().get(0);
        }

        if (!managers.containsKey(result.getId())) {
            log.debug("create manager {} for quiz {}", result.getId(), quiz.getId());
            QuizResult fResult = result;
            managers.computeIfAbsent(result.getId(),
                    (k) -> QuizManagerFactory.getInstance().createManager(this, fResult));
        }
        QuizParticipant participant = quizParticipantRepository.getByQuizResultAndUser(result, user).orElse(null);
        if (participant == null) {
            participant = getManager(result).register(result, user);
            participant = quizParticipantRepository.save(participant);
            result.getParticipants().add(participant);
            result.setParticipantsNumber(result.getParticipants().size());
            log.debug("add participant {} for manager {}", participant.getId(), result.getId());
            sendForAll(result);
        }
        return participant;
    }

    private void sendForAll(QuizResult result) {
        result.getParticipants().forEach((participant -> {
            QuizBean bean = new QuizBean(result.getQuiz());
            bean.setResult(new QuizResultBean(participant, result));
            try {
                sendMessage(participant, new ResponseBean(bean));
            } catch (Exception e) {
                log.warn("error sending message to " + participant.getId(), e);
            }
        }));
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
        log.debug("register connection of participant {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresentOrElse(quizParticipant -> {
            QuizResult result = quizParticipant.getQuizResult();
            if (result.getStatus() == QuizResultStatus.FINISHED) {
                sendMessage(participantId, session,
                        new ResponseBean(new QuizResultBean(quizParticipant, result)));
                return;
            }
            sessions.put(participantId, session);
            getManager(result).join(quizParticipant);
        }, () -> log.debug("participant {} not found", participantId));
    }

    QuizAnswer findActiveAnswer(QuizParticipant quizParticipant) {
        return quizAnswerRepository.findByQuizResultAndAnswererAndStatus(
                quizParticipant.getQuizResult(),
                quizParticipant.getUser(),
                QuizAnswerStatus.ACTIVE).orElse(null);
    }

    void addAnswer(QuizParticipant quizParticipant, Question question) {
        log.debug("select question {} for participant {}", question.getId(), quizParticipant.getId());
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
        WebSocketSession session = sessions.get(participant.getId());
        if (session != null) {
            sendMessage(participant.getId(), session, bean);
        } else {
            log.warn("cannot send to {} message {}: not connected", participant.getId(), bean);
        }
    }

    private void sendMessage(Long participantId, WebSocketSession session, ResponseBean bean) {
        log.debug("send {} message {}", participantId, bean);
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
        log.debug("answer #{} participant {}", answer, participantId);
        quizParticipantRepository.findById(participantId).ifPresent(
                (quizParticipant) -> getManager(quizParticipant.getQuizResult()).answer(quizParticipant, answer));
    }

    @Override
    @Transactional
    public void next(Long participantId) {
        log.debug("next question for participant {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(
                quizParticipant -> getManager(quizParticipant.getQuizResult()).next(quizParticipant));
    }

    private TextMessage toMessage(Object bean)
            throws JsonProcessingException {
        return new TextMessage(new ObjectMapper().writeValueAsString(bean));
    }

    @Override
    public void disconnect(Long participantId) {
        log.debug("unregister connection of participant {}", participantId);
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

    public List<Question> listQuestions(Club club, Set<Long> used, int limit) {
        if (used.isEmpty()) {
            return questionRepository.findAllByClub(club, PageRequest.of(0, limit)).getContent();
        } else {
            return questionRepository.findAllByClubBut(club, used, PageRequest.of(0, limit));
        }
    }
}
