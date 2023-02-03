package com.bouchov.quiz;

import com.bouchov.quiz.entities.*;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MainControllerTest {
    private static final String PWD = "AkljdsAS12";
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    private User testUser;
    @Autowired
    private PasswordEncoder encoder;

    @BeforeEach
    public void setUp() {
        User user = new User(UniqSource.uniqueString("login"),
                UniqSource.uniqueString("nickname"),
                encoder.encode(PWD), UserRole.PLAYER);
        testUser = userRepository.save(user);
    }

    @Test
    public void testLogin() throws Exception {
        HttpSession session = login()
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getRequest()
                .getSession(false);
        Assertions.assertNotNull(session, "session must exist");
    }

    public ResultActions login() throws Exception {
        return mvc.perform(MockMvcRequestBuilders.post("/")
                .param("login", testUser.getLogin())
                .param("password", PWD)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print());
    }

    @Test
    public void testLoginBadPassword() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/")
                .param("login", testUser.getLogin())
                .param("password", PWD+1)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andDo(print());
    }

    @Test
    public void testRegister() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/register")
                .param("login", UniqSource.uniqueString("login"))
                .param("nickname", UniqSource.uniqueString("nickname"))
                .param("password", UniqSource.uniqueString("password"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testPing()
            throws Exception {
        MockHttpSession session = (MockHttpSession) login().andReturn().getRequest().getSession(false);

        mvc.perform(MockMvcRequestBuilders.get("/ping")
                        .session(session)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testPingUnauthorized()
            throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/ping")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
