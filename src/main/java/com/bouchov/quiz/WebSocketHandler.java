package com.bouchov.quiz;

import com.bouchov.quiz.protocol.RequestBean;
import com.bouchov.quiz.services.QuizService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WebSocketHandler extends TextWebSocketHandler {
    private final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    @Autowired
    private QuizService service;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        Long participantId = (Long) session.getAttributes().get(SessionAttributes.PARTICIPANT_ID);;
        if (participantId != null) {
            service.unregister(participantId);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        super.handleTextMessage(session, message);
        RequestBean request = new ObjectMapper().readValue(message.getPayload(), RequestBean.class);
        logger.debug("received message: {}", request);
        if (request.getEnter() != null) {
            Long participantId = request.getEnter().getParticipantId();
            service.start(participantId, session);
            session.getAttributes().put(SessionAttributes.PARTICIPANT_ID, participantId);
        } else if (request.getAnswer() != null) {
            Long participantId = (Long) session.getAttributes().get(SessionAttributes.PARTICIPANT_ID);
            service.answer(participantId, request.getAnswer());
        } else if (request.getNext() != null) {
            Long participantId = (Long) session.getAttributes().get(SessionAttributes.PARTICIPANT_ID);
            service.next(participantId);
        }
    }
}
