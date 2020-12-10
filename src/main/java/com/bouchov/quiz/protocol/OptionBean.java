package com.bouchov.quiz.protocol;

public class OptionBean {
    private int id;
    private String name;

    public OptionBean() {
    }

    public OptionBean(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "OptionBean{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
