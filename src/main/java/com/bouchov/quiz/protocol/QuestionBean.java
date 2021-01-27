package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.Option;
import com.bouchov.quiz.entities.Question;

import java.util.List;

public class QuestionBean {
    private Long id;
    private Long categoryId;
    private String category;
    private String text;
    private Integer answer;
    private int value;
    private Integer number;
    private Integer total;
    private boolean selected;
    private List<Option> options;

    public QuestionBean() {
    }

    public QuestionBean(Question question) {
        this(question, question.getOptions(), null, null);
    }

    public QuestionBean(Question question, List<Option> options, Integer number, Integer total) {
        this.id = question.getId();
        this.categoryId = question.getCategory().getId();
        this.category = question.getCategory().getName();
        this.text = question.getText();
        this.answer = question.getAnswer();
        this.value = question.getValue();
        this.options = options;
        this.number = number;
        this.total = total;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public void setValue(int value) {
        this.value = value;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return "[QuestionBean " +
                "id=" + id +
                ", categoryId=" + categoryId +
                ", category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", answer=" + answer +
                ", value=" + value +
                ", number=" + number +
                ", total=" + total +
                ", selected=" + selected +
                ", options=" + options +
                ']';
    }
}
