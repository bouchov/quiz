package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.QuizParticipant;
import com.bouchov.quiz.entities.QuizResult;
import com.bouchov.quiz.entities.User;

public interface QuizManager {
    QuizParticipant register(QuizResult result, User user);

    void join(QuizParticipant participant);

    void answer(QuizParticipant participant, int answer);

    void next(QuizParticipant participant);
}
