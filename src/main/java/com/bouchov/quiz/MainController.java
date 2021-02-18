package com.bouchov.quiz;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import com.bouchov.quiz.entities.UserRole;
import com.bouchov.quiz.protocol.UserBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Objects;

@RestController
@RequestMapping("/")
class MainController extends AbstractController {
    private final UserRepository userRepository;
    private final HttpSession session;

    @Autowired
    public MainController(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
    }

    @GetMapping
    public ModelAndView get() {
        return new ModelAndView("redirect:/index.html");
    }

    @PostMapping
    public UserBean login(
            @RequestParam String login,
            @RequestParam String password) {
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));
        if (userId != null && !Objects.equals(user.getId(), userId)) {
            session.invalidate();
            throw new UserNotFoundException(login);
        }
        if (!Objects.equals(password, user.getPassword())) {
            throw new UserNotFoundException(login);
        }
        session.setAttribute(SessionAttributes.USER_ID, user.getId());
        session.setAttribute(SessionAttributes.USER_ROLE, user.getRole());
        return new UserBean(user);
    }

    @PostMapping("/register")
    public UserBean register(
            @RequestParam String login,
            @RequestParam String nickname,
            @RequestParam String password) {
        User user = userRepository.findByLogin(login).orElse(null);
        if (user != null) {
            throw new UserAlreadyExistsException(login);
        }
        user = userRepository.findByNickname(nickname).orElse(null);
        if (user != null) {
            throw new UserAlreadyExistsException(nickname);
        }
        user = new User(login, nickname, password, UserRole.PLAYER);
        user = userRepository.save(user);

        session.setAttribute(SessionAttributes.USER_ID, user.getId());
        session.setAttribute(SessionAttributes.USER_ROLE, user.getRole());
        return new UserBean(user);
    }

    @RequestMapping("/ping")
    public UserBean ping() {
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        if (userId == null) {
            throw new UserNotFoundException("session expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return new UserBean(user);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class UserNotFoundException extends RuntimeException {

        public UserNotFoundException(String login) {
            super("could not find user '" + login + "'.");
        }

        public UserNotFoundException(Long userId) {
            super("could not find user " + userId + ".");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    static class UserAlreadyExistsException extends RuntimeException {

        public UserAlreadyExistsException(String login) {
            super("user '" + login + "' already exists.");
        }
    }

}
