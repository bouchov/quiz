package com.bouchov.quiz.init;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.UserBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Set;

public class UserLoader {
    private final String fileName;
    private final PasswordEncoder encoder;

    public UserLoader(String fileName, PasswordEncoder encoder) {
        this.fileName = fileName;
        this.encoder = encoder;
    }

    public UserLoader(PasswordEncoder encoder) {
        this("users.json", encoder);
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
                        encode(user.getPassword()),
                        user.getRole());
                entity.setClubs(Set.of(club));
                repository.save(entity);
                enterClubRepository.save(new EnterClubRequest(entity, club, EnterClubStatus.ACCEPTED));
            }
        }
    }

    private String encode(String password) {
        return encoder.encode(password);
    }
}
