package io.github.MLNaiba.simpleinvoice.security;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class AuthenticationIntegrationTests {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String INVALID_PASSWORD = "invalid-password";

    private static final String BASE_URL = "/api/auth/login";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void authenticate_givenValidCredentials_shouldReturnOk() throws Exception {

        // ARRANGE

        AuthenticationRequest request = new AuthenticationRequest(
                USERNAME,
                PASSWORD
        );

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    public void authenticate_givenInvalidCredentials_shouldReturnUnauthorized() throws Exception {

        // ARRANGE

        AuthenticationRequest request = new AuthenticationRequest(
                USERNAME,
                INVALID_PASSWORD
        );

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // ASSERT

        response.andExpect(status().isUnauthorized());
    }

    // ---

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        User user = User.builder()
                .username(USERNAME)
                .password(passwordEncoder.encode(PASSWORD))
                .role(UserRole.USER)
                .build();

        userRepository.save(user);
    }
}
