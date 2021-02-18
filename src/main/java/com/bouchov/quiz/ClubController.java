package com.bouchov.quiz;

import com.bouchov.quiz.entities.Club;
import com.bouchov.quiz.entities.ClubRepository;
import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import com.bouchov.quiz.protocol.ClubBean;
import com.bouchov.quiz.protocol.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/club")
class ClubController extends AbstractController {
    private final HttpSession session;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;

    @Autowired
    public ClubController(HttpSession session,
            UserRepository userRepository,
            ClubRepository clubRepository) {
        this.session = session;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
    }

    @GetMapping("/{clubUid}")
    public ClubBean getClub(@PathVariable String clubUid) {
        Club club = clubRepository.findByUid(clubUid)
                .orElseThrow(() -> new ClubNotFoundException(clubUid));
        return new ClubBean(club);
    }

    @PostMapping("/list")
    public PageBean<ClubBean> listClubs() {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        List<Club> ownedClubs = clubRepository.findAllByOwner(user);
        Set<Club> set = new HashSet<>(user.getClubs());
        set.addAll(ownedClubs);
        PageBean<ClubBean> bean = new PageBean<>(1, set.size(), set.size());
        bean.setElements(set.stream().map(ClubBean::new).collect(Collectors.toList()));
        return bean;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ClubNotFoundException extends RuntimeException {

        public ClubNotFoundException(Long clubId) {
            super("could not find club '" + clubId + "'.");
        }

        public ClubNotFoundException(String clubName) {
            super("could not find club '" + clubName + "'.");
        }
    }
}
