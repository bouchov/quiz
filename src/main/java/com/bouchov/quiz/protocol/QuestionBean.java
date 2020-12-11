package com.bouchov.quiz.protocol;

import java.util.List;

public class QuestionBean {
    private String category;
    private String text;
    private Integer answer;
    private List<OptionBean> options;

    public QuestionBean() {
    }

    public QuestionBean(String category, String text, Integer answer, List<OptionBean> options) {
        this.category = category;
        this.text = text;
        this.answer = answer;
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
                ", answer=" + answer +
                ", options=" + options +
                '}';
    }
}
