package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.ParticipantStatus;
import com.bouchov.quiz.entities.QuizParticipant;

public class QuizResultBean {
    private Long id;
    private int place;
    private int right;
    private int wrong;
    private ParticipantStatus status;
    private int value;

    public QuizResultBean(QuizParticipant participant) {
        this.id = participant.getId();
        this.place = participant.getPlace();
        this.right = participant.getRightAnswers();
        this.wrong = participant.getWrongAnswers();
        this.value = participant.getValue();
        this.status = participant.getStatus();
    }

    public QuizResultBean() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getWrong() {
        return wrong;
    }

    public void setWrong(int wrong) {
        this.wrong = wrong;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "QuizResultBean{" +
                "id=" + id +
                ", place=" + place +
                ", right=" + right +
                ", wrong=" + wrong +
                ", status=" + status +
                ", value=" + value +
                '}';
    }
}
