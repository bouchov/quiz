package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.ParticipantStatus;

public class QuizResultBean {
    private int right;
    private int wrong;
    private ParticipantStatus status;
    private int value;

    public QuizResultBean(int right, int wrong, int value, ParticipantStatus status) {
        this.right = right;
        this.wrong = wrong;
        this.value = value;
        this.status = status;
    }

    public QuizResultBean() {
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
                "right=" + right +
                ", wrong=" + wrong +
                ", status=" + status +
                ", value=" + value +
                '}';
    }
}
