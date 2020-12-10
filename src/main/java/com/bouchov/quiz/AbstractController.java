package com.bouchov.quiz;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpSession;
import java.util.Optional;

public class AbstractController {
    protected void checkAuthorization(HttpSession session) {
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        if (userId == null) {
            throw new AuthorizationRequiredException();
        }
    }

    protected Optional<User> getUser(HttpSession session, UserRepository repository) {
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        if (userId == null) {
            return Optional.empty();
        }
        return repository.findById(userId);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class AuthorizationRequiredException extends RuntimeException {
        public AuthorizationRequiredException() {
        }
    }
}
