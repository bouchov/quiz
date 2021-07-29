package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Alexandre Y. Bouchov
 * Date: 24.02.2021
 * Time: 11:53
 * Copyright 2014 ConnectiveGames LLC. All rights reserved.
 */
@Entity
public class QuizResult extends BasicEntity {
    @ManyToOne
    private Quiz quiz;
    private QuizResultStatus status;
    @Column(nullable = false)
    private Date registrationStarted;
    private Date started;
    private Date finished;
    private int participantsNumber;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizParticipant> participants;

    public QuizResult() {
    }

    public QuizResult(Quiz quiz, QuizResultStatus status, Date registrationStarted) {
        this.quiz = quiz;
        this.status = status;
        this.registrationStarted = registrationStarted;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public QuizResultStatus getStatus() {
        return status;
    }

    public void setStatus(QuizResultStatus status) {
        this.status = status;
    }

    public Date getRegistrationStarted() {
        return registrationStarted;
    }

    public Date getStarted() {
        return started;
    }

    public void setStarted(Date started) {
        this.started = started;
    }

    public Date getFinished() {
        return finished;
    }

    public void setFinished(Date finished) {
        this.finished = finished;
    }

    public int getParticipantsNumber() {
        return participantsNumber;
    }

    public void setParticipantsNumber(int participantsNumber) {
        this.participantsNumber = participantsNumber;
    }

    public List<QuizParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<QuizParticipant> participants) {
        this.participants = participants;
    }

    @Override
    public String toString() {
        return "[QuizResult super=" + super.toString() +
                ", quiz=" + quiz +
                ", status=" + status +
                ", registrationStarted=" + registrationStarted +
                ", started=" + started +
                ", finished=" + finished +
                ", participantsNumber=" + participantsNumber +
                ", participants=" + participants +
                ']';
    }
}
