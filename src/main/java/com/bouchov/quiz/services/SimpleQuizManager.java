package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.protocol.ResponseBean;

import java.util.Date;

public class SimpleQuizManager extends AbstractQuizManager {
    public SimpleQuizManager(QuizServiceImpl service) {
        super(service);
    }

    @Override
    public QuizParticipant register(Quiz quiz, User user) {
        if (quiz.getParticipants().isEmpty()) {
            if (quiz.getStatus() == QuizStatus.ACTIVE) {
                quiz.setStatus(QuizStatus.STARTED);
                quiz.setStartedDate(new Date());
            }
            return new QuizParticipant(quiz, user, ParticipantStatus.ACTIVE);
        } else {
            throw new RuntimeException("too many users");
        }
    }

    @Override
    public void join(QuizParticipant participant) {
        QuizAnswer answer = service.findActiveAnswer(participant);
        Question question;
        if (answer == null) {
            question = nextQuestion(participant);
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
        if (quizAnswer.getQuestion().getAnswer() == answer) {
            quizAnswer.setStatus(QuizAnswerStatus.SUCCESS);
            quizAnswer.setValue(quizAnswer.getQuestion().getValue());
            participant.setRightAnswers(participant.getRightAnswers() + 1);
        } else {
            quizAnswer.setStatus(QuizAnswerStatus.FAILED);
            quizAnswer.setValue(0);
            participant.setWrongAnswers(participant.getWrongAnswers() + 1);
        }
        participant.setValue(participant.getValue() + quizAnswer.getValue());

        service.sendMessage(participant, new ResponseBean(quizAnswer.getStatus()));
    }

    @Override
    public void next(QuizParticipant participant) {
        Question selectedQuestion = nextQuestion(participant);
        if (selectedQuestion == null) {
            participant.getQuiz().setStatus(QuizStatus.FINISHED);
            participant.setStatus(ParticipantStatus.FINISHED);
            service.sendMessage(participant, new ResponseBean(new QuizResultBean(participant)));
        } else {
            service.addAnswer(participant, selectedQuestion);
            service.sendMessage(participant, new ResponseBean(toQuestion(selectedQuestion)));
        }
    }

}
