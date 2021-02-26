package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
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

    protected void checkAdmin(HttpSession session) {
        checkAuthorization(session);
        UserRole role = (UserRole) session.getAttribute(SessionAttributes.USER_ROLE);
        if (role != UserRole.ADMIN) {
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

    protected Optional<Club> getClub(HttpSession session, ClubRepository repository) {
        Long clubId = (Long) session.getAttribute(SessionAttributes.CLUB_ID);
        if (clubId == null) {
            return Optional.empty();
        }
        return repository.findById(clubId);
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    static class AuthorizationRequiredException extends RuntimeException {
        public AuthorizationRequiredException() {
        }
    }
}
