package com.bouchov.quiz.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.Arrays;
import java.util.List;

@Entity
public class Question extends BasicEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Club club;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Category category;
    private String text;
    private int answer;
    private int value;
    private String optionsJson;

    public Question() {
    }

    public Question(Club club, Category category, String text, int answer, int value, String optionsJson) {
        this.club = club;
        this.category = category;
        this.text = text;
        this.answer = answer;
        this.value = value;
        this.optionsJson = optionsJson;
    }

    public Club getClub() {
        return club;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOptionsJson() {
        return optionsJson;
    }

    public void setOptionsJson(String optionsJson) {
        this.optionsJson = optionsJson;
    }

    public List<Option> getOptions() {
        return toOptions(optionsJson);
    }

    private static List<Option> toOptions(String json) {
        if (json == null) {
            return null;
        }
        try {
            return Arrays.asList(new ObjectMapper().readValue(json, Option[].class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
