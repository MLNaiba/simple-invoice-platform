package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateUserRequest;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;
import io.github.MLNaiba.simpleinvoice.exception.ResourceAlreadyExistsException;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.service.JwtService;
import io.github.MLNaiba.simpleinvoice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTests {

    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String INVALID_ID = "invalid-id";
    private static final String BLANK_USERNAME = "";
    private static final String BLANK_PASSWORD = "";

    private static final String BASE_URL = "/api/users";
    private static final String ID_URL = BASE_URL + "/%s";

    private static final String VALIDATION_FAILED = "Validation failed";
    private static final String USERNAME_REQUIRED = "username: Username must not be blank";
    private static final String PASSWORD_REQUIRED = "password: Password must not be blank";
    private static final String USERNAME_EXISTS = "User with username [%s] already exists";
    private static final String ID_NOT_FOUND = "User with id [%s] not found";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    // Security filter dependencies required by @WebMvcTest
    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    public void createUser_givenValidRequest_shouldReturnCreatedUser() throws Exception {

        // ARRANGE

        CreateUserRequest createUserRequest = createRequest();
        UserResponse userResponse = response();

        given(userService.createUser(createUserRequest))
                .willReturn(userResponse);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // ASSERT

        response.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.username").value(userResponse.username()));

        verify(userService).createUser(createUserRequest);
    }

    @ParameterizedTest
    @MethodSource("invalidUserData")
    public void createUser_givenInvalidRequestBadParameters_shouldReturnBadRequest(
            String username,
            String password,
            List<String> expectedErrors
    ) throws Exception {

        // ARRANGE

        CreateUserRequest createUserRequest =
                new CreateUserRequest(username, password);

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // ASSERT

        response.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(VALIDATION_FAILED))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors", hasSize(expectedErrors.size())));

        for (String expectedError : expectedErrors) {
            response.andExpect(jsonPath("$.errors", hasItem(expectedError)));
        }

        verify(userService, never()).createUser(any());
    }

    @Test
    public void createUser_givenInvalidRequestExistingUsername_shouldReturnResourceAlreadyExists()
            throws Exception {

        // ARRANGE

        CreateUserRequest createUserRequest = createRequest();

        given(userService.createUser(createUserRequest))
                .willThrow(new ResourceAlreadyExistsException("User", createUserRequest.username()));

        // ACT

        ResultActions response = mockMvc.perform(post(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createUserRequest)));

        // ASSERT

        response.andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value(USERNAME_EXISTS.formatted(createUserRequest.username())))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).createUser(createUserRequest);
    }

    @Test
    public void getUserById_givenValidRequest_shouldReturnUser() throws Exception {

        // ARRANGE

        UserResponse userResponse = response();

        given(userService.getUserById(userResponse.id()))
                .willReturn(userResponse);

        // ACT

        ResultActions response = mockMvc.perform(get(ID_URL.formatted(ID)));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(userResponse.id()))
                .andExpect(jsonPath("$.username").value(userResponse.username()));

        verify(userService).getUserById(userResponse.id());

    }

    @Test
    public void getUserById_givenInvalidRequestNonexistentId_shouldReturnNotFound()
            throws Exception {

        // ARRANGE

        given(userService.getUserById(INVALID_ID))
                .willThrow(new ResourceNotFoundException("User", INVALID_ID));

        // ACT

        ResultActions response = mockMvc.perform(get(ID_URL.formatted(INVALID_ID)));

        // ASSERT

        response.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(String.format(ID_NOT_FOUND, INVALID_ID)))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(userService).getUserById(INVALID_ID);
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllUsers_givenValidRequest_shouldReturnUsers(
            int responseCount
    ) throws Exception {

        // ARRANGE

        List<UserResponse> userResponses =
                IntStream.range(0, responseCount)
                        .mapToObj(this::response)
                        .toList();

        given(userService.getAllUsers())
                .willReturn(userResponses);

        // ACT

        ResultActions response = mockMvc.perform(get(BASE_URL));

        // ASSERT

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(responseCount));

        for (int i = 0; i < responseCount; ++i) {
            UserResponse userResponse = userResponses.get(i);

            response
                    .andExpect(jsonPath("$[%d].id".formatted(i))
                            .value(userResponse.id()))
                    .andExpect(jsonPath("$[%d].username".formatted(i))
                            .value(userResponse.username()));
        }

        verify(userService).getAllUsers();
    }

    // ---

    private static Stream<Arguments> invalidUserData() {
        return Stream.of(
                Arguments.of(
                        USERNAME,
                        BLANK_PASSWORD,
                        List.of(PASSWORD_REQUIRED)
                ),
                Arguments.of(
                        BLANK_USERNAME,
                        PASSWORD,
                        List.of(USERNAME_REQUIRED)
                ),
                Arguments.of(
                        BLANK_USERNAME,
                        BLANK_PASSWORD,
                        List.of(USERNAME_REQUIRED, PASSWORD_REQUIRED)
                )
        );
    }

    // ---

    private CreateUserRequest createRequest() {
        return new CreateUserRequest(
                USERNAME,
                PASSWORD);
    }

    private UserResponse response(int num) {
        return new UserResponse(
                ID + num,
                USERNAME + num
        );
    }

    private UserResponse response() {
        return new UserResponse(
                ID,
                USERNAME
        );
    }
}
