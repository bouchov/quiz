package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/")
class MainController extends AbstractController {
    private final UserRepository userRepository;
    private final QuizParticipantRepository quizParticipantRepository;
    private final PasswordEncoder encoder;
    private final HttpSession session;

    @Autowired
    public MainController(UserRepository userRepository,
            QuizParticipantRepository quizParticipantRepository,
            PasswordEncoder encoder,
            HttpSession session) {
        this.userRepository = userRepository;
        this.quizParticipantRepository = quizParticipantRepository;
        this.encoder = encoder;
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
        if (!encoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException(login);
        }
        login(user);
        return new UserBean(user);
    }

    private void login(User user) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getLogin(), null,
                List.of(new SimpleGrantedAuthority(user.getRole().roleName())));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
        session.setAttribute(SessionAttributes.USER_ID, user.getId());
        session.setAttribute(SessionAttributes.USER_ROLE, user.getRole());
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
        user = new User(login, nickname, encoder.encode(password), UserRole.PLAYER);
        user = userRepository.save(user);

        login(user);
        return new UserBean(user);
    }

    @RequestMapping("/ping")
    public SessionBean ping(Principal principal) {
        String login = principal.getName();
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> new UserNotFoundException(login));
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
