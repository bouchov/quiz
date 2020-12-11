package com.bouchov.quiz;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
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
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        if (userId == null) {
            return new ModelAndView("forward:/login");
        }
        return new ModelAndView("forward:/quiz");
    }

    @GetMapping("/login")
    public ModelAndView login() {
        return new ModelAndView("redirect:/login.html");
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
        return new UserBean(user);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class UserNotFoundException extends RuntimeException {

        public UserNotFoundException(String login) {
            super("could not find user '" + login + "'.");
        }
    }

}
