package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.UserBean;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

public class UserLoader {
    private final String fileName;

    public UserLoader(String fileName) {
        this.fileName = fileName;
    }

    public UserLoader() {
        this("users.json");
    }

    public void load(UserRepository repository,
            EnterClubRequestRepository enterClubRepository, Club club) throws IOException {
        try(InputStream stream = UserLoader.class.getResourceAsStream(fileName)) {
            Objects.requireNonNull(stream, "file not found");
            ObjectMapper mapper = new ObjectMapper();
            UserBean[] beans = mapper.readValue(stream, UserBean[].class);
            for (UserBean user : beans) {
                User entity = new User(
                        user.getLogin(),
                        user.getNickname(),
                        user.getPassword(),
                        user.getRole());
                entity.setClubs(Set.of(club));
                repository.save(entity);
                enterClubRepository.save(new EnterClubRequest(entity, club, EnterClubStatus.ACCEPTED));
            }
        }
    }
}
