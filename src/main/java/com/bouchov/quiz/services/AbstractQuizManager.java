package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuestionBean;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Collections.shuffle;

public abstract class AbstractQuizManager implements QuizManager {
    protected final QuizServiceImpl service;

    protected AbstractQuizManager(QuizServiceImpl service) {
        this.service = service;
    }

    protected Question nextQuestion(QuizParticipant participant) {
        Set<Long> used = participant.getAnswers().stream()
                .map((qa) -> qa.getQuestion().getId())
                .collect(Collectors.toSet());
        Question selectedQuestion = null;
        for (Question question : participant.getQuiz().getQuestions()) {
            if (!used.contains(question.getId())) {
                selectedQuestion = question;
                break;
            }
        }
        return selectedQuestion;
    }

    protected QuestionBean toQuestion(Question question) {
        ArrayList<Option> options = new ArrayList<>(question.getOptions());
        shuffle(options);
        return new QuestionBean(question, options);
    }

    protected void checkAnswerAndSaveResult(QuizParticipant participant, int answer, QuizAnswer quizAnswer) {
        quizAnswer.setAnswer(answer);
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
    }
}
