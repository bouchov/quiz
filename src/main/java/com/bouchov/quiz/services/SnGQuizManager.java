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
    private final Logger log = LoggerFactory.getLogger(SnGQuizManager.class);
    private volatile Future<?> task;
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
                    log.debug("[{}] start at {}", resultId, startTime);
                }
            }
        }
        return participant;
    }

    protected void start() {
        QuizResult result = service.getQuizResult(resultId);
        result.setStatus(QuizResultStatus.STARTED);
        log.debug("[{}] start", resultId);
        Quiz quiz = result.getQuiz();
        Question question = null;
        for (QuizParticipant participant : result.getParticipants()) {
            QuizAnswer answer = service.findActiveAnswer(participant);
            if (answer != null) {
                question = answer.getQuestion();
                log.debug("[{}] continue question {}", resultId, question.getId());
                break;
            }
        }
        if (question == null) {
            for (QuizParticipant participant : result.getParticipants()) {
                if (question == null) {
                    question = nextQuestion(quiz.getClub(), participant);
                    if (question == null) {
                        break;
                    }
                }
                log.debug("[{}] next question {}", resultId, question.getId());
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
        log.debug("[{}] finish", resultId);
        QuizResult result = service.getQuizResult(resultId);
        result.setStatus(QuizResultStatus.FINISHED);
        List<QuizParticipant> participants = new ArrayList<>(result.getParticipants());
        participants.sort(Comparator.comparingInt(QuizParticipant::getValue).reversed());
        int place = 0, counter = 0;
        Integer value = null;
        for (QuizParticipant participant : participants) {
            participant.setStatus(ParticipantStatus.FINISHED);
            counter++;
            if (value == null || value != participant.getValue()) {
                place = counter;
            }
            participant.setPlace(place);
            value = participant.getValue();
            service.sendMessage(participant, new ResponseBean(new QuizResultBean(participant, result)));
        }
    }

    @Override
    public void join(QuizParticipant participant) {
        log.debug("[{}] join participant {}", resultId, participant.getId());
        QuizAnswer answer = service.findActiveAnswer(participant);
        List<QuizAnswer> answers = participant.getAnswers();
        int number = answers.size();
        if (answer == null) {
            if (!participant.getAnswers().isEmpty()) {
                answer = answers.get(number - 1);
            }
        }
        if (answer != null) {
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
        log.debug("[{}] answer participant {}", resultId, participant.getId());
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
        log.debug("[{}] next", resultId);
        QuizResult result = service.getQuizResult(resultId);
        Quiz quiz = result.getQuiz();
        Question selectedQuestion = null;
        for (QuizParticipant participant : result.getParticipants()) {
            if (selectedQuestion == null) {
                selectedQuestion = nextQuestion(quiz.getClub(), participant);
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
