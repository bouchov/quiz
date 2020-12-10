package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.OptionBean;
import com.bouchov.quiz.protocol.QuestionBean;
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
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Collections.shuffle;

@Service
class QuizServiceImpl implements QuizService, DisposableBean, InitializingBean {
    private final Logger log = LoggerFactory.getLogger(QuizServiceImpl.class);

    private final QuizParticipantRepository quizParticipantRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final Map<Long, WebSocketSession> sessions;

    @Autowired
    public QuizServiceImpl(QuizParticipantRepository quizParticipantRepository,
                           QuizAnswerRepository quizAnswerRepository) {
        this.quizParticipantRepository = quizParticipantRepository;
        this.quizAnswerRepository = quizAnswerRepository;
        sessions = new ConcurrentHashMap<>();
    }

    @Override
    public QuizParticipant register(Quiz quiz, User user) {
        QuizParticipant participant = quizParticipantRepository.getByQuizAndUser(quiz, user).orElse(null);
        if (participant == null) {
            participant = quizParticipantRepository.save(new QuizParticipant(quiz, user, ParticipantStatus.ACTIVE));
        }
        return participant;
    }

    @Override
    @Transactional
    public void start(Long participantId, WebSocketSession session) {
        log.debug("register session for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(quizParticipant -> {
            Quiz quiz = quizParticipant.getQuiz();
            if (quiz.getStatus() == QuizStatus.ACTIVE) {
                quiz.setStatus(QuizStatus.STARTED);
            } else if (quiz.getStatus() == QuizStatus.FINISHED) {
                sendMessage(session, new ResponseBean(toQuizResult(quizParticipant)));
                return;
            } else if (quiz.getStatus() != QuizStatus.STARTED) {
                throw new IllegalStateException("invalid quiz status: " + quiz.getStatus());
            }
            sessions.put(participantId, session);
            QuizAnswer answer = findActiveAnswer(quizParticipant);
            Question question;
            if (answer == null) {
                question = nextQuestion(quizParticipant);
            } else {
                question = answer.getQuestion();
            }
            selectQuestionAndSend(session, quizParticipant, question);
        });
    }

    private QuizAnswer findActiveAnswer(QuizParticipant quizParticipant) {
        return quizAnswerRepository.findByQuizAndAnswererAndStatus(quizParticipant.getQuiz(),
                quizParticipant.getUser(),
                QuizAnswerStatus.ACTIVE).orElse(null);
    }

    private void selectQuestionAndSend(WebSocketSession session,
                                       QuizParticipant quizParticipant,
                                       Question question) {
        log.debug("select question #{} for {}", question.getId(), quizParticipant.getId());
        User user = quizParticipant.getUser();
        if (quizAnswerRepository.findByQuestionAndAnswerer(question, user).isEmpty()) {
            QuizAnswer answer = quizAnswerRepository.save(new QuizAnswer(
                    quizParticipant.getQuiz(),
                    question,
                    user,
                    -1,
                    0,
                    QuizAnswerStatus.ACTIVE));
            quizParticipant.getAnswers().add(answer);
        }
        sendMessage(session, new ResponseBean(toQuestion(question)));
    }

    private void sendMessage(WebSocketSession session, ResponseBean bean) {
        try {
            if (session != null) {
                session.sendMessage(toMessage(bean));
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Override
    @Transactional
    public void answer(Long participantId, int answer) {
        log.debug("answer #{} for {}", answer, participantId);
        quizParticipantRepository.findById(participantId).ifPresent((quizParticipant) -> {
            QuizAnswer quizAnswer = findActiveAnswer(quizParticipant);
            if (quizAnswer == null) {
                throw new IllegalStateException("no active question found for " + participantId);
            }
            if (quizAnswer.getQuestion().getAnswer() == answer) {
                quizAnswer.setStatus(QuizAnswerStatus.SUCCESS);
                quizAnswer.setValue(quizAnswer.getQuestion().getValue());
                quizParticipant.setRightAnswers(quizParticipant.getRightAnswers() + 1);
                quizParticipant.setValue(quizParticipant.getValue() + quizAnswer.getValue());
            } else {
                quizAnswer.setStatus(QuizAnswerStatus.FAILED);
                quizAnswer.setValue(0);
                quizParticipant.setWrongAnswers(quizParticipant.getWrongAnswers() + 1);
                quizParticipant.setValue(quizParticipant.getValue() + quizAnswer.getValue());
            }
            WebSocketSession session = sessions.get(participantId);
            sendMessage(session, new ResponseBean(quizAnswer.getStatus()));
            // TODO: 10.12.2020 schedule next question in X secs
        });
    }

    @Override
    @Transactional
    public void next(Long participantId) {
        log.debug("next question for {}", participantId);
        quizParticipantRepository.findById(participantId).ifPresent(quizParticipant -> {
            Question selectedQuestion = nextQuestion(quizParticipant);
            WebSocketSession session = sessions.get(participantId);
            if (selectedQuestion == null) {
                quizParticipant.getQuiz().setStatus(QuizStatus.FINISHED);
                quizParticipant.setStatus(ParticipantStatus.FINISHED);
                sendMessage(session, new ResponseBean(toQuizResult(quizParticipant)));
            } else {
                selectQuestionAndSend(session, quizParticipant, selectedQuestion);
            }
        });
    }

    private QuizResultBean toQuizResult(QuizParticipant participant) {
        return new QuizResultBean(participant.getRightAnswers(),
                participant.getWrongAnswers(),
                participant.getValue(),
                participant.getStatus());
    }

    private Question nextQuestion(QuizParticipant quizParticipant) {
        Set<Long> used = new HashSet<>();
        Iterable<QuizAnswer> answers = quizAnswerRepository.findAllByQuizAndAnswerer(quizParticipant.getQuiz(),
                quizParticipant.getUser());
        answers.forEach(quizAnswer -> used.add(quizAnswer.getQuestion().getId()));
        Question selectedQuestion = null;
        for (Question question : quizParticipant.getQuiz().getQuestions()) {
            if (!used.contains(question.getId())) {
                selectedQuestion = question;
                break;
            }
        }
        return selectedQuestion;
    }

    private QuestionBean toQuestion(Question question) {
        QuestionBean bean = new QuestionBean();
        bean.setCategory(question.getCategory().getName());
        bean.setText(question.getText());
        bean.setOptions(new ArrayList<>());
        for (int i = 0; i < question.getOptions().size(); i++) {
            bean.getOptions().add(new OptionBean(i, question.getOptions().get(i)));
        }
        shuffle(bean.getOptions());
        return bean;
    }

    private TextMessage toMessage(Object bean) throws JsonProcessingException {
        return new TextMessage(new ObjectMapper().writeValueAsString(bean));
    }

    @Override
    public void unregister(Long participantId) {
        log.debug("unregister session for {}", participantId);
        sessions.remove(participantId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("INITIALIZED");
    }

    @Override
    public void destroy() throws Exception {
        log.info("DESTROY");
    }
}
