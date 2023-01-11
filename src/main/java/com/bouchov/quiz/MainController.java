package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.protocol.SessionBean;
import com.bouchov.quiz.protocol.UserBean;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/")
class MainController extends AbstractController {
    private final UserRepository userRepository;
    private final QuizParticipantRepository quizParticipantRepository;
    private final HttpSession session;

    @Autowired
    public MainController(UserRepository userRepository,
            QuizParticipantRepository quizParticipantRepository,
            HttpSession session) {
        this.userRepository = userRepository;
        this.quizParticipantRepository = quizParticipantRepository;
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
            throw new NeedReLoginException();
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
    public SessionBean ping() {
        Long userId = (Long) session.getAttribute(SessionAttributes.USER_ID);
        if (userId == null) {
            throw new UserNotFoundException("session expired");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        List<QuizParticipant> participants = quizParticipantRepository.findAllByUserAndStatus(user,
                ParticipantStatus.ACTIVE);
        List<QuizBean> games;
        if (!participants.isEmpty()) {
            games = new ArrayList<>();
            participants.forEach((participant) -> {
                QuizResult result = participant.getQuizResult();
                QuizBean bean = new QuizBean(result.getQuiz());
                bean.setResult(new QuizResultBean(participant, result));
                games.add(bean);
            });
        } else {
            games = null;
        }
        return new SessionBean(new UserBean(user), games);
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

    @ResponseStatus(HttpStatus.CONFLICT)
    static class NeedReLoginException extends RuntimeException {
        public NeedReLoginException() {
            super("user is logged in with another credentials");
        }
    }

}
