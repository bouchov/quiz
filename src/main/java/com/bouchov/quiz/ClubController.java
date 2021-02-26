package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.ClubBean;
import com.bouchov.quiz.protocol.ClubFilterBean;
import com.bouchov.quiz.protocol.ClubRequestBean;
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
import java.util.Optional;

@RestController
@RequestMapping("/club")
class ClubController extends AbstractController {
    private final HttpSession session;
    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final EnterClubRequestRepository requestRepository;

    @Autowired
    public ClubController(HttpSession session,
            UserRepository userRepository,
            ClubRepository clubRepository,
            EnterClubRequestRepository requestRepository) {
        this.session = session;
        this.userRepository = userRepository;
        this.clubRepository = clubRepository;
        this.requestRepository = requestRepository;
    }

    @GetMapping("/{clubId}")
    public ClubBean selectClub(@PathVariable Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ClubNotFoundException(clubId));
        session.setAttribute(SessionAttributes.CLUB_ID, club.getId());
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

    @PostMapping("/create")
    public ClubBean create(@RequestBody ClubBean bean) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Club club = clubRepository.save(new Club(
                bean.getName(),
                IdGenerator.generate(),
                user,
                bean.isAutoInclusion()));
        user.getClubs().add(club);
        userRepository.save(user);
        return new ClubBean(club, true);
    }

    @PostMapping("/enter")
    public ClubRequestBean enter(@RequestBody ClubBean bean) {
        checkAuthorization(session);
        Club club;
        if (bean.getId() != null) {
            club = clubRepository.findById(bean.getId()).orElseThrow((() -> new ClubNotFoundException(bean.getId())));
        } else if (bean.getUid() != null) {
            club = clubRepository.findByUid(bean.getUid()).orElseThrow((() -> new ClubNotFoundException(bean.getUid())));
        } else if (bean.getName() != null) {
            club = clubRepository.findByName(bean.getName()).orElseThrow((() -> new ClubNotFoundException(bean.getName())));
        } else {
            throw new ClubNotFoundException("empty parameters");
        }
        EnterClubStatus status;
        User user = getUser(session, userRepository).orElseThrow();
        if (user.getClubs().contains(club)) {
            status = EnterClubStatus.SUCCESS;
        } else {
            Optional<EnterClubRequest> request = requestRepository.findByUserAndClub(user, club);
            if (request.isEmpty()) {
                if (club.isAutoInclusion()) {
                    status = EnterClubStatus.SUCCESS;
                    user.getClubs().add(club);
                    userRepository.save(user);
                } else {
                    status = EnterClubStatus.PENDING;
                    requestRepository.save(new EnterClubRequest(user, club, EnterClubStatus.PENDING));
                }
            } else {
                status = request.get().getStatus();
            }
        }
        return new ClubRequestBean(new ClubBean(club), status);
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
