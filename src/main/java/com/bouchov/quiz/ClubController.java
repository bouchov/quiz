package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import com.bouchov.quiz.protocol.*;
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
        PageRequest pageable = toPageable(filter, Sort.by("name"));
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
                bean.isAutoInclusion() != null && bean.isAutoInclusion()));
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
            status = EnterClubStatus.ACCEPTED;
        } else {
            Optional<EnterClubRequest> request = requestRepository.findByUserAndClub(user, club);
            if (request.isEmpty()) {
                if (club.isAutoInclusion()) {
                    status = EnterClubStatus.ACCEPTED;
                    user.getClubs().add(club);
                    userRepository.save(user);
                } else {
                    status = EnterClubStatus.PENDING;
                }
                requestRepository.save(new EnterClubRequest(user, club, status));
            } else {
                status = request.get().getStatus();
            }
        }
        return new ClubRequestBean(new ClubBean(club), status);
    }

    @PostMapping("/requestStatus")
    public void acceptOrResign(@RequestBody ChangedCollectionBean changes) {
        if (changes.getAdded() != null && !changes.getAdded().isEmpty()) {
            for (Long id : changes.getAdded()) {
                EnterClubRequest request = requestRepository.findById(id).orElseThrow();
                EnterClubStatus status = request.getStatus();
                if (status != EnterClubStatus.ACCEPTED) {
                    request.setStatus(EnterClubStatus.ACCEPTED);
                    requestRepository.save(request);
                    User user = request.getUser();
                    user.getClubs().add(request.getClub());
                    userRepository.save(user);
                }
            }
        }
        if (changes.getRemoved() != null && !changes.getRemoved().isEmpty()) {
            for (Long id : changes.getRemoved()) {
                EnterClubRequest request = requestRepository.findById(id).orElseThrow();
                EnterClubStatus status = request.getStatus();
                if (status != EnterClubStatus.RESIGNED) {
                    request.setStatus(EnterClubStatus.RESIGNED);
                    requestRepository.save(request);
                    if (status == EnterClubStatus.ACCEPTED) {
                        User user = request.getUser();
                        user.getClubs().remove(request.getClub());
                        userRepository.save(user);
                    }
                }
            }
        }
    }

    @PostMapping("/requests")
    public PageBean<EnterClubRequestBean> requests(@RequestBody EnterClubRequestFilterBean filter) {
        checkAuthorization(session);
        User user = getUser(session, userRepository).orElseThrow();
        Club club = getClub(session, clubRepository).orElseThrow();
        if (isOwner(club, user) == null) {
            throw new ClubNotFoundException("user not an owner of club");
        }
        PageRequest pageable = toPageable(filter, Sort.by("user"));
        EnterClubStatus[] status = filter.getStatus();
        if (status == null || status.length == 0) {
            status = EnterClubStatus.values();
        }
        Page<EnterClubRequest> page  = requestRepository.findAllByClubAndStatus(club, status, pageable);
        PageBean<EnterClubRequestBean> bean =
                new PageBean<>(page.getNumber(), page.getSize(), page.getTotalPages());
        bean.setElements(page.map(EnterClubRequestBean::new).getContent());
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
            super("club '" + clubName + "' not found");
        }
    }
}
