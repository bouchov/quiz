package com.bouchov.quiz.entities;

public enum UserRole {
    PLAYER,
    ADMIN;

    public String roleName() {
        return "ROLE_" + name();
    }
}
