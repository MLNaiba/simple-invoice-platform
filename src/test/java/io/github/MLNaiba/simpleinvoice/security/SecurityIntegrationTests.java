package io.github.MLNaiba.simpleinvoice.security;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationResponse;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTests {

    private static final String TEST_USERNAME = "username";
    private static final String TEST_PASSWORD = "password";

    private static final String ENDPOINT_CUSTOMERS = "/api/customers";
    private static final String ENDPOINT_PRODUCTS = "/api/products";
    private static final String ENDPOINT_INVOICES = "/api/invoices";
    private static final String ENDPOINT_USERS = "/api/users";
    
    private static final String BEARER_PREFIX = "Bearer ";

    private static final String ID_SUFFIX = "/1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AdminProperties adminProperties;

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void getResources_givenInvalidToken_shouldReturnUnauthorized(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(get(resourceEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + "not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void unknownEndpoint_givenAdminToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/not-a-valid-endpoint")
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void getResources_givenUnauthenticatedRequest_shouldReturnUnauthorized(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(get(resourceEndpoint))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void getResources_givenUserToken_shouldReturnOk(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(get(resourceEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void getResources_givenAdminToken_shouldReturnOk(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(get(resourceEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void postResources_givenUserToken_shouldReturnForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(post(resourceEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void postResources_givenAdminToken_shouldReturnNotForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(post(resourceEndpoint)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void putResources_givenUserToken_shouldReturnForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(put(resourceEndpoint + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void putResources_givenAdminToken_shouldReturnNotForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(put(resourceEndpoint + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void deleteResources_givenUserToken_shouldReturnForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(delete(resourceEndpoint + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @MethodSource("resourceEndpoints")
    public void deleteResources_givenAdminToken_shouldReturnNotForbidden(
            String resourceEndpoint
    ) throws Exception {
        mockMvc.perform(delete(resourceEndpoint + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    public void postUsers_givenUnauthenticatedRequest_shouldReturnNotForbidden() throws Exception {
        mockMvc.perform(post(ENDPOINT_USERS))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    public void postUsers_givenUserToken_shouldReturnNotForbidden() throws Exception {
        mockMvc.perform(post(ENDPOINT_USERS)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    public void postUsers_givenAdminToken_shouldReturnNotForbidden() throws Exception {
        mockMvc.perform(post(ENDPOINT_USERS)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    public void getUsers_givenUnauthenticatedRequest_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getUsers_givenUserToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUsers_givenAdminToken_shouldReturnOk() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().isOk());
    }

    @Test
    public void getUserById_givenUnauthenticatedRequest_shouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS + ID_SUFFIX))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getUserById_givenUserToken_shouldReturnForbidden() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainUserToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUserById_givenAdminToken_shouldReturnNotForbidden() throws Exception {
        mockMvc.perform(get(ENDPOINT_USERS + ID_SUFFIX)
                        .header(HttpHeaders.AUTHORIZATION, BEARER_PREFIX + obtainAdminToken()))
                .andExpect(status().is(not(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    public void getApiDocs_givenUnauthenticatedRequest_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    // ---

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();

        User admin = User.builder()
                .username(adminProperties.username())
                .password(passwordEncoder.encode(adminProperties.password()))
                .role(UserRole.ADMIN)
                .build();

        User user = User.builder()
                .username(TEST_USERNAME)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .role(UserRole.USER)
                .build();

        userRepository.save(admin);
        userRepository.save(user);
    }

    // ---

    private static Stream<String> resourceEndpoints() {
        return Stream.of(
                ENDPOINT_CUSTOMERS,
                ENDPOINT_PRODUCTS,
                ENDPOINT_INVOICES
        );
    }

    // ---

    private String obtainToken(String username, String password) throws Exception {

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(username, password);

        ResultActions response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)));

        response.andExpect(status().isOk());

        AuthenticationResponse authenticationResponse = objectMapper.readValue(
                response.andReturn().getResponse().getContentAsString(),
                AuthenticationResponse.class
        );

        return authenticationResponse.token();
    }

    private String obtainAdminToken() throws Exception {
        return obtainToken(
                adminProperties.username(),
                adminProperties.password()
        );
    }

    private String obtainUserToken() throws Exception {
        return obtainToken(
                TEST_USERNAME,
                TEST_PASSWORD
        );
    }
}
