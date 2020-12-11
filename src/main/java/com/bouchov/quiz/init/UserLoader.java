package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import com.bouchov.quiz.protocol.UserBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class UserLoader {
    private final String fileName;

    public UserLoader(String fileName) {
        this.fileName = fileName;
    }

    public UserLoader() {
        this("users.json");
    }

    public void load(UserRepository repository) throws IOException {
        try(InputStream stream = UserLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            UserBean[] beans = mapper.readValue(stream, UserBean[].class);
            for (UserBean user : beans) {
                repository.save(new User(user.getLogin(), user.getNickname(), user.getPassword(), user.getRole()));
            }
        }
    }
}
