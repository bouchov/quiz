package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class QuizParticipant extends BasicEntity {
    @ManyToOne(fetch = FetchType.EAGER)
    private QuizResult quizResult;
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizAnswer> answers;
    private int place;
    @Column(name = "part_value")
    private int value;
    private int rightAnswers;
    private int wrongAnswers;
    private ParticipantStatus status;

    public QuizParticipant() {
    }

    public QuizParticipant(QuizResult quizResult, User user, ParticipantStatus status) {
        this.quizResult = quizResult;
        this.user = user;
        this.status = status;
    }

    public QuizResult getQuizResult() {
        return quizResult;
    }

    public User getUser() {
        return user;
    }

    public ParticipantStatus getStatus() {
        return status;
    }

    public void setStatus(ParticipantStatus status) {
        this.status = status;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<QuizAnswer> answers) {
        this.answers = answers;
    }

    public int getPlace() {
        return place;
    }

    public void setPlace(int place) {
        this.place = place;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public int getRightAnswers() {
        return rightAnswers;
    }

    public void setRightAnswers(int rightAnswers) {
        this.rightAnswers = rightAnswers;
    }

    public int getWrongAnswers() {
        return wrongAnswers;
    }

    public void setWrongAnswers(int wrongAnswers) {
        this.wrongAnswers = wrongAnswers;
    }

    @Override
    public String toString() {
        return "[QuizParticipant" +
                " quizResult=" + quizResult +
                ", user=" + user +
                ", place=" + place +
                ", value=" + value +
                ", rightAnswers=" + rightAnswers +
                ", wrongAnswers=" + wrongAnswers +
                ", status=" + status +
                ']';
    }
}

