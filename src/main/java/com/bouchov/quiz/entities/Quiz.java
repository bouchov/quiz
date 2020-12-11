package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
public class Quiz extends BasicEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User author;
    private String name;
    private QuizType type;
    private int minPlayers;
    private int maxPlayers;
    private Date startDate;
    private Date startedDate;
    private QuizStatus status;
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Question> questions;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizParticipant> participants;

    public Quiz() {
    }

    public Quiz(User author,
                String name,
                QuizType type,
                int minPlayers,
                int maxPlayers,
                Date startDate,
                Date startedDate,
                QuizStatus status) {
        this.author = author;
        this.name = name;
        this.type = type;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startDate = startDate;
        this.startedDate = startedDate;
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers(int minPlayers) {
        this.minPlayers = minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartedDate() {
        return startedDate;
    }

    public void setStartedDate(Date startedDate) {
        this.startedDate = startedDate;
    }

    public QuizStatus getStatus() {
        return status;
    }

    public void setStatus(QuizStatus status) {
        this.status = status;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public List<QuizParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<QuizParticipant> participants) {
        this.participants = participants;
    }
}
