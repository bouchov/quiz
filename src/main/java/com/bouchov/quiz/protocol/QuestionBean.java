package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.Option;
import com.bouchov.quiz.entities.Question;

import java.util.List;

public class QuestionBean {
    private String category;
    private String text;
    private Integer answer;
    private int value;
    private List<Option> options;

    public QuestionBean() {
    }

    public QuestionBean(Question question) {
        this(question, question.getOptions());
    }

    public QuestionBean(Question question, List<Option> options) {
        this.category = question.getCategory().getName();
        this.text = question.getText();
        this.answer = question.getAnswer();
        this.value = question.getValue();
        this.options = options;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getAnswer() {
        return answer;
    }

    public void setAnswer(Integer answer) {
        this.answer = answer;
    }

    public int getValue() {
        return value;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "QuestionBean{" +
                "category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", answer=" + answer +
                ", value=" + value +
                ", options=" + options +
                '}';
    }
}
