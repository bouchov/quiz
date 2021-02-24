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
    private final Long resultId;

    public SnGQuizManager(QuizServiceImpl service, QuizResult result) {
        super(service);
        this.resultId = result.getId();
    }

    @Override
    public QuizParticipant register(QuizResult result, User user) {
        int size = result.getParticipants().size();
        Quiz quiz = result.getQuiz();
        QuizParticipant participant = null;
        if (size < quiz.getMaxPlayers()) {
            participant = new QuizParticipant(result, user, ParticipantStatus.ACTIVE);

            if (size + 1 == quiz.getMinPlayers()) {
                if (task == null) {
                    Instant startTime;
                    if (size + 1 == quiz.getMaxPlayers()) {
                        startTime = Instant.now().plus(Duration.of(5L, ChronoUnit.SECONDS));
                    } else {
                        //wait for others 1 min
                        startTime = Instant.now().plus(Duration.of(1L, ChronoUnit.MINUTES));
                    }
                    result.setStarted(new Date(startTime.toEpochMilli()));
                    task = service.schedule(this::start, startTime);
                }
            }
        }
        return participant;
    }

    protected void start() {
        QuizResult result = service.getQuizResult(resultId);
        result.setStatus(QuizResultStatus.STARTED);
        Quiz quiz = result.getQuiz();
        Question question = null;
        for (QuizParticipant participant : result.getParticipants()) {
            QuizAnswer answer = service.findActiveAnswer(participant);
            if (answer != null) {
                question = answer.getQuestion();
                break;
            }
        }
        if (question == null) {
            for (QuizParticipant participant : result.getParticipants()) {
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
                int number = participant.getAnswers().size();
                Integer total = null;
                if (quiz.getSelectionStrategy() != QuestionSelectionStrategy.ALL) {
                    total = quiz.getQuestionsNumber();
                }
                if (answer.isEmpty() || answer.get().getStatus() == QuizAnswerStatus.ACTIVE) {
                    service.sendMessage(participant, new ResponseBean(toQuestion(question, number, total)));
                } else {
                    service.sendMessage(participant,
                            new ResponseBean(new AnswerBean(answer.get(), toQuestion(question, number, total))));
                }
            }
        }
        if (question == null) {
            finishQuiz();
        }
    }

    private void finishQuiz() {
        QuizResult result = service.getQuizResult(resultId);
        result.setStatus(QuizResultStatus.FINISHED);
        List<QuizParticipant> participants = new ArrayList<>(result.getParticipants());
        participants.sort(Comparator.comparingInt(QuizParticipant::getValue));
        int place = participants.size();
        for (QuizParticipant participant : participants) {
            participant.setStatus(ParticipantStatus.FINISHED);
            participant.setPlace(place--);
            service.sendMessage(participant, new ResponseBean(new QuizResultBean(participant, result)));
        }
    }

    @Override
    public void join(QuizParticipant participant) {
        QuizAnswer answer = service.findActiveAnswer(participant);
        if (answer != null) {
            int number = participant.getAnswers().size();
            Integer total = null;
            Quiz quiz = participant.getQuizResult().getQuiz();
            if (quiz.getSelectionStrategy() != QuestionSelectionStrategy.ALL) {
                total = quiz.getQuestionsNumber();
            }
            if (answer.getStatus() == QuizAnswerStatus.ACTIVE) {
                service.sendMessage(participant, new ResponseBean(toQuestion(answer.getQuestion(), number, total)));
            } else {
                service.sendMessage(participant,
                        new ResponseBean(new AnswerBean(answer, toQuestion(answer.getQuestion(), number, total))));
            }
        } else {
            QuizResult result = participant.getQuizResult();
            QuizBean bean = new QuizBean(result.getQuiz());
            bean.setResult(new QuizResultBean(participant, result));
            service.sendMessage(participant, new ResponseBean(bean));
        }
    }

    @Override
    public void answer(QuizParticipant participant, int answer) {
        QuizAnswer quizAnswer = service.findActiveAnswer(participant);
        if (quizAnswer == null) {
            throw new IllegalStateException("no active question found for " + participant);
        }
        checkAnswerAndSaveResult(participant, answer, quizAnswer);

        int number = participant.getAnswers().size();
        Integer total = null;
        QuizResult result = service.getQuizResult(resultId);
        Quiz quiz = result.getQuiz();
        if (quiz.getSelectionStrategy() != QuestionSelectionStrategy.ALL) {
            total = quiz.getQuestionsNumber();
        }
        service.sendMessage(participant,
                new ResponseBean(new AnswerBean(quizAnswer, toQuestion(quizAnswer.getQuestion(), number, total))));
        int size = result.getParticipants().size();
        int answered = 0;
        for (QuizParticipant quizParticipant : result.getParticipants()) {
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
        QuizResult result = service.getQuizResult(resultId);
        Quiz quiz = result.getQuiz();
        Question selectedQuestion = null;
        for (QuizParticipant participant : result.getParticipants()) {
            if (selectedQuestion == null) {
                selectedQuestion = nextQuestion(participant);
            }
            if (selectedQuestion == null) {
                finishQuiz();
                break;
            } else {
                service.addAnswer(participant, selectedQuestion);
                int number = participant.getAnswers().size();
                Integer total = null;
                if (quiz.getSelectionStrategy() != QuestionSelectionStrategy.ALL) {
                    total = quiz.getQuestionsNumber();
                }
                service.sendMessage(participant, new ResponseBean(toQuestion(selectedQuestion, number, total)));
            }
        }
    }

    @Override
    public void next(QuizParticipant participant) {
    }
}
