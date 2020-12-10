package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class QuizParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.EAGER)
    private Quiz quiz;
    @ManyToOne(fetch = FetchType.EAGER)
    private User user;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizAnswer> answers;
    private int value;
    private int rightAnswers;
    private int wrongAnswers;
    private ParticipantStatus status;

    public QuizParticipant() {
    }

    public QuizParticipant(Quiz quiz, User user, ParticipantStatus status) {
        this.quiz = quiz;
        this.user = user;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Quiz getQuiz() {
        return quiz;
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
}

