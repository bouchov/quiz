package com.bouchov.quiz.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "users")
@Entity
public class User extends BasicEntity {
    @Column(unique = true)
    private String login;
    @Column(unique = true)
    private String nickname;
    private String password;
    private UserRole role;

    public User() {
    }

    public User(String login, String nickname, String password, UserRole role) {
        this.login = login;
        this.nickname = nickname;
        this.password = password;
        this.role = role;
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
}
