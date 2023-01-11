package com.bouchov.quiz.entities;

import jakarta.persistence.*;

import java.util.Set;

@Table(name = "users")
@Entity
public class User extends BasicEntity {
    @Column(unique = true)
    private String login;
    @Column(unique = true)
    private String nickname;
    private String password;
    private UserRole role;
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Club> clubs;

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

    public Set<Club> getClubs() {
        return clubs;
    }

    public void setClubs(Set<Club> clubs) {
        this.clubs = clubs;
    }

    @Override
    public String toString() {
        return "[User" +
                " login='" + login + '\'' +
                ", nickname='" + nickname + '\'' +
                ", password='" + password + '\'' +
                ", role=" + role +
                ']';
    }
}
