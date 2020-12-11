package com.bouchov.quiz.services;

import com.bouchov.quiz.entities.Quiz;
import com.bouchov.quiz.entities.QuizParticipant;
import com.bouchov.quiz.entities.User;
import org.springframework.web.socket.WebSocketSession;

public interface QuizService {
    QuizParticipant register(Quiz quiz, User user);

    void connect(Long participantId, WebSocketSession session);

    void disconnect(Long participantId);

    void answer(Long participantId, int answer);

    void next(Long participantId);
}
