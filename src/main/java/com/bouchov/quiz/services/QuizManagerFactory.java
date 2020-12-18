package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.Quiz;
import com.bouchov.quiz.entities.QuizType;

public class QuizManagerFactory {
    private static final QuizManagerFactory instance = new QuizManagerFactory();

    private QuizManagerFactory() {
    }

    public static QuizManagerFactory getInstance() {
        return instance;
    }

    public QuizManager createManager(QuizServiceImpl service, Quiz quiz) {
        if (quiz.getType() == QuizType.SIMPLE) {
            if (quiz.getMaxPlayers() == 1) {
                return new SimpleQuizManager(service);
            } else {
                return new SnGQuizManager(service, quiz);
            }
        }
        throw new UnsupportedOperationException("createManager");
    }
}
