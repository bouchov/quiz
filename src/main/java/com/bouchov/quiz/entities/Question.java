package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.List;

@Entity
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;
    private String text;
    private int answer;
    private int value;
    @Type(type = "json")
    @Column(columnDefinition = "json", nullable = false)
    private List<Option> options;

    public Question() {
    }

    public Question(Category category, String text, int answer, int value, List<Option> options) {
        this.category = category;
        this.text = text;
        this.answer = answer;
        this.value = value;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public int getAnswer() {
        return answer;
    }

    public void setAnswer(int answer) {
        this.answer = answer;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
