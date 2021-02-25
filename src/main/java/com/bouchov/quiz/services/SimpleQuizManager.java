package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.AnswerBean;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.protocol.ResponseBean;

import java.util.Date;

public class SimpleQuizManager extends AbstractQuizManager {
    private final Long resultId;

    public SimpleQuizManager(QuizServiceImpl service, QuizResult result) {
        super(service);
        this.resultId = result.getId();
    }

    @Override
    public QuizParticipant register(QuizResult result, User user) {
        if (result.getParticipants().isEmpty()) {
            if (result.getStatus() == QuizResultStatus.REGISTER) {
                result.setStatus(QuizResultStatus.STARTED);
                result.setStarted(new Date());
            }
            return new QuizParticipant(result, user, ParticipantStatus.ACTIVE);
        } else {
            throw new RuntimeException("too many users");
        }
    }

    @Override
    public void join(QuizParticipant participant) {
        QuizAnswer answer = service.findActiveAnswer(participant);
        Question question;
        if (answer == null) {
            Club club = service.getQuizResult(resultId).getQuiz().getClub();
            question = nextQuestion(club, participant);
            service.addAnswer(participant, question);
        } else {
            question = answer.getQuestion();
        }
        service.sendMessage(participant, new ResponseBean(toQuestion(question)));
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
    }

    @Override
    public void next(QuizParticipant participant) {
        Club club = service.getQuizResult(resultId).getQuiz().getClub();
        Question selectedQuestion = nextQuestion(club, participant);
        if (selectedQuestion == null) {
            QuizResult result = participant.getQuizResult();
            result.setStatus(QuizResultStatus.FINISHED);
            participant.setStatus(ParticipantStatus.FINISHED);
            service.sendMessage(participant, new ResponseBean(new QuizResultBean(participant, result)));
        } else {
            service.addAnswer(participant, selectedQuestion);
            service.sendMessage(participant, new ResponseBean(toQuestion(selectedQuestion)));
        }
    }

}
