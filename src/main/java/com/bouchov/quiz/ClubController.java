package com.bouchov.quiz;

import com.bouchov.quiz.entities.Club;
import com.bouchov.quiz.entities.ClubRepository;
import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import com.bouchov.quiz.protocol.ClubBean;
import com.bouchov.quiz.protocol.ClubFilterBean;
import com.bouchov.quiz.protocol.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Objects;

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
        return getUser(session, userRepository)
                .map(user -> new ClubBean(club, isOwner(club, user)))
                .orElseGet(() -> new ClubBean(club));
    }

    @PostMapping("/list")
    public PageBean<ClubBean> listClubs(@RequestBody ClubFilterBean filter) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        int pageNumber = filter.getPage() == null ? 0 : filter.getPage();
        int pageSize = filter.getSize() == null ? 10 : filter.getSize();
        Sort sort = Sort.by("name");
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Club> page;
        if (filter.getName() == null || filter.getName().isEmpty()) {
            page = clubRepository.findAllByParticipants(user, pageable);
        } else {
            page  = clubRepository.findAllByParticipantsAndNameUpper(user,
                    filter.getName().toUpperCase(Locale.ROOT), pageable);
        }
        PageBean<ClubBean> bean = new PageBean<>(page.getNumber(), page.getSize(), page.getTotalPages());
        bean.setElements(page.map((c) -> new ClubBean(c, isOwner(c, user))).getContent());
        return bean;
    }

    private static Boolean isOwner(Club club, User user) {
        if (Objects.equals(user, club.getOwner())) {
            return Boolean.TRUE;
        }
        return null;
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class ClubNotFoundException extends RuntimeException {

        public ClubNotFoundException(Long clubId) {
            super("club " + clubId + " not found");
        }

        public ClubNotFoundException(String clubName) {
            super("club '" + clubName + "' not find");
        }
    }
}
