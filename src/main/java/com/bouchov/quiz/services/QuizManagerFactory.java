package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.QuizResult;
import com.bouchov.quiz.entities.QuizType;

public class QuizManagerFactory {
    private static final QuizManagerFactory instance = new QuizManagerFactory();

    private QuizManagerFactory() {
    }

    public static QuizManagerFactory getInstance() {
        return instance;
    }

    public QuizManager createManager(QuizServiceImpl service, QuizResult result) {
        if (result.getQuiz().getType() == QuizType.SIMPLE) {
            return new SnGQuizManager(service, result);
        }
        throw new UnsupportedOperationException("createManager");
    }
}
