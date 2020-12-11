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
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
class QuizServiceImpl implements QuizService, DisposableBean, InitializingBean {
    private final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizParticipantRepository quizParticipantRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final Map<Long, WebSocketSession> sessions;
    private final Map<Long, QuizManager> managers;

    @Autowired
    public QuizServiceImpl(QuizParticipantRepository quizParticipantRepository,
                           QuizAnswerRepository quizAnswerRepository) {
        this.quizParticipantRepository = quizParticipantRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        sessions = new ConcurrentHashMap<>();
        managers = new ConcurrentHashMap<>();
    }

    @Override
    public QuizParticipant register(Quiz quiz, User user) {
        if (quiz.getStatus() == QuizStatus.DRAFT) {
            throw new RuntimeException("quiz is not ready");
        }
        QuizParticipant participant = quizParticipantRepository.getByQuizAndUser(quiz, user).orElse(null);
        if (quiz.getStatus() == QuizStatus.FINISHED) {
            if (participant == null) {
                throw new RuntimeException("quiz is finished");
            } else {
                return participant;
            }
        }
        if (!managers.containsKey(quiz.getId())) {
            managers.computeIfAbsent(quiz.getId(),
                    (k) -> QuizManagerFactory.getInstance().createManager(this, quiz));
        }
        if (participant == null) {
            participant = getManager(quiz).register(quiz, user);
            participant = quizParticipantRepository.save(participant);
            quiz.getParticipants().add(participant);
        }
        return participant;
    }

    private QuizManager getManager(Quiz quiz) {
        QuizManager quizManager = managers.get(quiz.getId());
        if (quizManager == null) {
            throw new RuntimeException("manager not found for " + quiz);
        }
        return quizManager;
    }

    @Override
    @Transactional
    public void connect(Long participantId, WebSocketSession session) {
        log.debug("register session for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(quizParticipant -> {
            Quiz quiz = quizParticipant.getQuiz();
            if (quiz.getStatus() == QuizStatus.FINISHED) {
                sendMessage(session, new ResponseBean(new QuizResultBean(quizParticipant)));
                return;
            } else if (quiz.getStatus() != QuizStatus.ACTIVE
                    && quiz.getStatus() != QuizStatus.STARTED) {
                throw new IllegalStateException("invalid quiz status: " + quiz.getStatus());
            }
            sessions.put(participantId, session);
            getManager(quiz).join(quizParticipant);
        });
    }

    QuizAnswer findActiveAnswer(QuizParticipant quizParticipant) {
        return quizAnswerRepository.findByQuizAndAnswererAndStatus(quizParticipant.getQuiz(),
                quizParticipant.getUser(),
                QuizAnswerStatus.ACTIVE).orElse(null);
    }

    void addAnswer(QuizParticipant quizParticipant, Question question) {
        log.debug("select question #{} for {}", question.getId(), quizParticipant.getId());
        User user = quizParticipant.getUser();
        QuizAnswer answer = quizAnswerRepository.save(new QuizAnswer(
                quizParticipant.getQuiz(),
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
                (quizParticipant) -> getManager(quizParticipant.getQuiz()).answer(quizParticipant, answer));
    }

    @Override
    @Transactional
    public void next(Long participantId) {
        log.debug("next question for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(
                quizParticipant -> getManager(quizParticipant.getQuiz()).next(quizParticipant));
    }

    private TextMessage toMessage(Object bean) throws JsonProcessingException {
        return new TextMessage(new ObjectMapper().writeValueAsString(bean));
    }

    @Override
    public void disconnect(Long participantId) {
        log.debug("unregister session for {}", participantId);
        sessions.remove(participantId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("INITIALIZED");
        // TODO: 11.12.2020 load quiz and participants
    }

    @Override
    public void destroy() throws Exception {
        log.info("DESTROY");
        // TODO: 11.12.2020 stop managers?
    }
}
