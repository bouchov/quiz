package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.QuizBean;
import com.bouchov.quiz.protocol.QuizResultBean;
import com.bouchov.quiz.services.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/quiz")
class QuizController extends AbstractController {
    private final HttpSession session;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuizService quizService;

    @Autowired
    public QuizController(HttpSession session,
                          UserRepository userRepository,
                          QuizRepository quizRepository,
                          QuizService quizService) {
        this.session = session;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.quizService = quizService;
    }

    @RequestMapping("/{quizId}/register")
    public QuizResultBean startQuiz(@PathVariable Long quizId) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Quiz quiz = quizRepository.findById(quizId).orElseThrow(() -> new QuizNotException(quizId));
        return new QuizResultBean(quizService.register(quiz, user));
    }

    @RequestMapping("/list")
    public List<QuizBean> list(
            @RequestParam(required = false) String name) {
        var list = new ArrayList<QuizBean>();
        if (name == null || name.isEmpty()) {
            quizRepository.findAllByStatus(QuizStatus.ACTIVE, QuizStatus.STARTED).forEach(
                    (e) -> list.add(new QuizBean(e)));
        } else {
            quizRepository.findAllByNameAndStatus(name, QuizStatus.ACTIVE, QuizStatus.STARTED).forEach(
                    (e) -> list.add(new QuizBean(e)));
        }
        return list;
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    static class QuizNotException extends RuntimeException {
        public QuizNotException(Long id) {
            super("quiz " + id + " not found");
        }
    }
}
