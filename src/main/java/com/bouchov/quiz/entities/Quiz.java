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
    @ManyToOne(fetch = FetchType.LAZY)
    private Club club;
    @Column(unique = true)
    private String name;
    private QuizType type;
    private int minPlayers;
    private int maxPlayers;
    private QuestionSelectionStrategy selectionStrategy;
    private int questionsNumber;
    private Date created;
    private Date activated;
    private Date closed;
    private QuizStatus status;
    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Question> questions;

    public Quiz() {
    }

    public Quiz(User author,
            Club club,
            String name,
            QuizType type,
            int minPlayers,
            int maxPlayers,
            QuestionSelectionStrategy selectionStrategy,
            int questionsNumber,
            Date created,
            Date activated,
            Date closed,
            QuizStatus status) {
        this.author = author;
        this.club = club;
        this.name = name;
        this.type = type;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.selectionStrategy = selectionStrategy;
        this.questionsNumber = questionsNumber;
        this.created = created;
        this.activated = activated;
        this.closed = closed;
        this.status = status;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
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

    public QuestionSelectionStrategy getSelectionStrategy() {
        return selectionStrategy;
    }

    public void setSelectionStrategy(QuestionSelectionStrategy selectionStrategy) {
        this.selectionStrategy = selectionStrategy;
    }

    public int getQuestionsNumber() {
        return questionsNumber;
    }

    public void setQuestionsNumber(int questionsNumber) {
        this.questionsNumber = questionsNumber;
    }

    public Date getCreated() {
        return created;
    }

    public Date getActivated() {
        return activated;
    }

    public void setActivated(Date activated) {
        this.activated = activated;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
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
}
