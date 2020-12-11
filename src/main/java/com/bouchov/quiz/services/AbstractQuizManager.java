package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.Option;
import com.bouchov.quiz.entities.Question;
import com.bouchov.quiz.entities.QuizParticipant;
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
}
