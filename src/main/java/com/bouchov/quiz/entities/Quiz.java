package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.List;

@Entity
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private User author;
    private String name;
    private QuizType type;
    private QuizStatus status;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Question> questions;
    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    private List<QuizParticipant> participants;

    public Quiz() {
    }

    public Quiz(User author, String name, QuizType type, QuizStatus status) {
        this.author = author;
        this.name = name;
        this.type = type;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
