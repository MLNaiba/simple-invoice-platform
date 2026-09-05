package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.AuthenticationRequest;
import io.github.MLNaiba.simpleinvoice.dto.AuthenticationResponse;
import io.github.MLNaiba.simpleinvoice.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerTests {

    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String TOKEN = "token";

    private static final String BLANK_USERNAME = "";
    private static final String BLANK_PASSWORD = "";

    private static final String BASE_URL = "/api/auth/login";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String USERNAME_REQUIRED = "username: Username must not be blank";
    private static final String PASSWORD_REQUIRED = "password: Password must not be blank";
    private static final String BAD_CREDENTIALS = "Bad credentials";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    @Test
    public void authenticate_givenValidRequest_shouldReturnToken() throws Exception {

        // ARRANGE

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(USERNAME, PASSWORD);

        AuthenticationResponse authenticationResponse =
                new AuthenticationResponse(TOKEN);

        given(authenticationService.authenticate(authenticationRequest))
                .willReturn(authenticationResponse);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(TOKEN));

        verify(authenticationService).authenticate(authenticationRequest);
    }

    @ParameterizedTest
    @MethodSource("invalidAuthenticationData")
    public void authenticate_givenInvalidRequestBadParameters_shouldReturnBadRequest(
            String username,
            String password,
            List<String> expectedErrors
    ) throws Exception {

        // ARRANGE

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(username, password);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())));

        for (String expectedError : expectedErrors) {
            response.andExpect(jsonPath("$.errors", hasItem(expectedError)));
        }

        verify(authenticationService, never()).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    public void authenticate_givenInvalidRequestBadCredentials_shouldReturnUnauthorized()
            throws Exception {

        // ARRANGE

        AuthenticationRequest authenticationRequest =
                new AuthenticationRequest(USERNAME, PASSWORD);

        given(authenticationService.authenticate(authenticationRequest))
                .willThrow(new BadCredentialsException(BAD_CREDENTIALS));

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)));

        // ASSERT

        response.andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(BAD_CREDENTIALS))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(authenticationService).authenticate(authenticationRequest);
    }

    // ---

    private static Stream<Arguments> invalidAuthenticationData() {
        return Stream.of(
                Arguments.of(
                        BLANK_USERNAME,
                        PASSWORD,
                        List.of(USERNAME_REQUIRED)
                ),
                Arguments.of(
                        USERNAME,
                        BLANK_PASSWORD,
                        List.of(PASSWORD_REQUIRED)
                ),
                Arguments.of(
                        BLANK_USERNAME,
                        BLANK_PASSWORD,
                        List.of(USERNAME_REQUIRED, PASSWORD_REQUIRED)
                )
        );
    }
}
