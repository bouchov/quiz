package com.bouchov.quiz;

import com.bouchov.quiz.entities.User;
import com.bouchov.quiz.entities.UserRepository;
import com.bouchov.quiz.entities.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class MainControllerTest {
    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    private User testUser;

    @BeforeEach
    public void setUp() {
        User user = new User(UniqSource.uniqueString("login"),
                UniqSource.uniqueString("nickname"),
                "pwd", UserRole.PLAYER);
        testUser = userRepository.save(user);
    }

    @Test
    public void testLogin() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/")
                .param("login", testUser.getLogin())
                .param("password", testUser.getPassword())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
