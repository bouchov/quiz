package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.Quiz;

public class QuizManagerFactory {
    private static final QuizManagerFactory instance = new QuizManagerFactory();

    private QuizManagerFactory() {
    }

    public static QuizManagerFactory getInstance() {
        return instance;
    }

    public QuizManager createManager(QuizServiceImpl service, Quiz quiz) {
        if (quiz.getMaxPlayers() == 1) {
            return new SimpleQuizManager(service);
        }
        throw new UnsupportedOperationException("createManager");
    }
}
