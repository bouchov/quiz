package com.bouchov.quiz.protocol;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRole;

public class UserBean {
    private Long id;
    private String login;
    private String nickname;
    private String password;
    private UserRole role;

    public UserBean() {
    }

    public UserBean(User user) {
        this.id = user.getId();
        this.login = user.getLogin();
        this.nickname = user.getNickname();
        this.password = user.getPassword();
        this.role = user.getRole();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "UserBean{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                '}';
    }
}
