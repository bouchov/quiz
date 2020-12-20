package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.AnswerBean;
import com.bouchov.quiz.protocol.QuizBean;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.protocol.ResponseBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Future;

public class SnGQuizManager extends AbstractQuizManager {
    private final Logger logger = LoggerFactory.getLogger(SnGQuizManager.class);
    private Future<?> task;
    private final Long quizId;

    public SnGQuizManager(QuizServiceImpl service, Quiz quiz) {
        super(service);
        this.quizId = quiz.getId();
    }

    @Override
    public QuizParticipant register(Quiz quiz, User user) {
        int size = quiz.getParticipants().size();
        QuizParticipant participant = null;
        if (size < quiz.getMaxPlayers()) {
            participant = new QuizParticipant(quiz, user, ParticipantStatus.ACTIVE);

            if (size + 1 == quiz.getMinPlayers()) {
                if (task == null) {
                    Instant startTime;
                    if (size + 1 == quiz.getMaxPlayers()) {
                        startTime = Instant.now().plus(Duration.of(5L, ChronoUnit.SECONDS));
                    } else {
                        //wait for others 1 min
                        startTime = Instant.now().plus(Duration.of(1L, ChronoUnit.MINUTES));
                    }
                    quiz.setStartDate(new Date(startTime.toEpochMilli()));
                    task = service.schedule(this::start, startTime);
                }
            }
        }
        return participant;
    }

    protected void start() {
        Quiz quiz = service.getQuiz(quizId);
        quiz.setStatus(QuizStatus.STARTED);
        Question question = null;
        for (QuizParticipant participant : quiz.getParticipants()) {
            QuizAnswer answer = service.findActiveAnswer(participant);
            if (answer != null) {
                question = answer.getQuestion();
                break;
            }
        }
        if (question == null) {
            for (QuizParticipant participant : quiz.getParticipants()) {
                if (question == null) {
                    question = nextQuestion(participant);
                    if (question == null) {
                        break;
                    }
                }
                Question qq = question;
                Optional<QuizAnswer> answer = participant.getAnswers().stream()
                        .filter((a) -> Objects.equals(qq.getId(), a.getQuestion().getId())).findAny();
                if (answer.isEmpty()) {
                    service.addAnswer(participant, question);
                }
                if (answer.isEmpty() || answer.get().getStatus() == QuizAnswerStatus.ACTIVE) {
                    service.sendMessage(participant, new ResponseBean(toQuestion(question)));
                } else {
                    service.sendMessage(participant,
                            new ResponseBean(new AnswerBean(answer.get(), toQuestion(question))));
                }
            }
        }
        if (question == null) {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        Quiz quiz = service.getQuiz(quizId);
        List<QuizParticipant> participants = new ArrayList<>(quiz.getParticipants());
        participants.sort(Comparator.comparingInt(QuizParticipant::getValue));
        int place = participants.size();
        for (QuizParticipant participant : participants) {
            participant.getQuiz().setStatus(QuizStatus.FINISHED);
            participant.setStatus(ParticipantStatus.FINISHED);
            participant.setPlace(place--);
            service.sendMessage(participant, new ResponseBean(new QuizResultBean(participant)));
        }
    }

    @Override
    public void join(QuizParticipant participant) {
        QuizAnswer answer = service.findActiveAnswer(participant);
        if (answer != null) {
            if (answer.getStatus() == QuizAnswerStatus.ACTIVE) {
                service.sendMessage(participant, new ResponseBean(toQuestion(answer.getQuestion())));
            } else {
                service.sendMessage(participant,
                        new ResponseBean(new AnswerBean(answer, toQuestion(answer.getQuestion()))));
            }
        } else {
            service.sendMessage(participant, new ResponseBean(new QuizBean(participant.getQuiz())));
        }
    }

    @Override
    public void answer(QuizParticipant participant, int answer) {
        QuizAnswer quizAnswer = service.findActiveAnswer(participant);
        if (quizAnswer == null) {
            throw new IllegalStateException("no active question found for " + participant);
        }
        checkAnswerAndSaveResult(participant, answer, quizAnswer);

        service.sendMessage(participant,
                new ResponseBean(new AnswerBean(quizAnswer, toQuestion(quizAnswer.getQuestion()))));
        Quiz quiz = service.getQuiz(quizId);
        int size = quiz.getParticipants().size();
        int answered = 0;
        for (QuizParticipant quizParticipant : quiz.getParticipants()) {
            if (service.findActiveAnswer(quizParticipant) == null) {
                answered++;
            }
        }
        if (size == answered) {
            //all players answered - next question
            task = service.schedule(this::next, Instant.now().plus(Duration.of(3, ChronoUnit.SECONDS)));
        }
    }

    private void next() {
        Quiz quiz = service.getQuiz(quizId);
        Question selectedQuestion = null;
        for (QuizParticipant participant : quiz.getParticipants()) {
            if (selectedQuestion == null) {
                selectedQuestion = nextQuestion(participant);
            }
            if (selectedQuestion == null) {
                finishQuiz();
                break;
            } else {
                service.addAnswer(participant, selectedQuestion);
                service.sendMessage(participant, new ResponseBean(toQuestion(selectedQuestion)));
            }
        }
    }

    @Override
    public void next(QuizParticipant participant) {
    }
}
