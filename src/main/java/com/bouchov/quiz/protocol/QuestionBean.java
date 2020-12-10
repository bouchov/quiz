package com.bouchov.quiz.protocol;

import java.util.List;

public class QuestionBean {
    private String category;
    private String text;
    private List<OptionBean> options;

    public QuestionBean() {
    }

    public QuestionBean(String category, String text, List<OptionBean> options) {
        this.category = category;
        this.text = text;
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

    public List<OptionBean> getOptions() {
        return options;
    }

    public void setOptions(List<OptionBean> options) {
        this.options = options;
    }

    @Override
    public String toString() {
        return "QuestionBean{" +
                "category='" + category + '\'' +
                ", text='" + text + '\'' +
                ", options=" + options +
                '}';
    }
}
