package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.dto.CreateUserRequest;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;
import io.github.MLNaiba.simpleinvoice.exception.ResourceAlreadyExistsException;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.UserMapper;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import io.github.MLNaiba.simpleinvoice.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {

    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";

    private static final String ENCODED_PREFIX = "encoded-";

    private static final String INVALID_ID = "invalid-id";

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Test
    public void createUser_givenValidRequest_shouldReturnCreatedUser() {

        // ARRANGE

        User user = user();
        CreateUserRequest createUserRequest = createUserRequest();

        when(userRepository.save(any(User.class)))
                .thenReturn(user);

        // ACT

        UserResponse response = userService.createUser(createUserRequest);

        // ASSERT

        verify(userRepository).existsByUsername(user.getUsername());

        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(user);

        verify(passwordEncoder).encode(createUserRequest.password());

        verify(userMapper).toResponse(user);
    }

    @Test
    public void createUser_givenInvalidRequestExistingUsername_shouldThrowResourceAlreadyExistsException() {

        // ARRANGE

        User user = user();
        CreateUserRequest createUserRequest = createUserRequest();

        when(userRepository.existsByUsername(user.getUsername()))
                .thenReturn(true);

        // ACT / ASSERT

        assertThatThrownBy(() ->
                userService.createUser(createUserRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(createUserRequest.username());

        verify(userRepository).existsByUsername(user.getUsername());
        verify(userRepository, never()).save(any(User.class));

        verify(passwordEncoder, never()).encode(any(String.class));

        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    public void createUser_givenInvalidRequestDuplicateKeyOnSave_shouldThrowResourceAlreadyExistsException() {

        // ARRANGE

        User user = user();
        CreateUserRequest createUserRequest = createUserRequest();

        when(userRepository.save(any(User.class)))
                .thenThrow(new DuplicateKeyException("Duplicate username"));

        // ACT / ASSERT

        assertThatThrownBy(() ->
                userService.createUser(createUserRequest))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining(createUserRequest.username());

        verify(userRepository).existsByUsername(createUserRequest.username());
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertThat(capturedUser)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(user);

        verify(passwordEncoder).encode(createUserRequest.password());

        verify(userMapper, never()).toResponse(any(User.class));
    }

    @Test
    public void getUserById_givenValidId_shouldReturnUser() {

        // ARRANGE

        User user = user();

        when(userRepository.findById(user.getId()))
                .thenReturn(Optional.of(user));

        // ACT

        UserResponse response = userService.getUserById(user.getId());

        // ASSERT

        verify(userRepository).findById(user.getId());

        assertThat(response)
                .usingRecursiveComparison()
                .isEqualTo(user);

        verify(userMapper).toResponse(user);
    }

    @Test
    public void getUserById_givenInvalidId_shouldThrowResourceNotFoundException() {

        // ARRANGE

        when(userRepository.findById(INVALID_ID))
                .thenReturn(Optional.empty());

        // ACT / ASSERT

        assertThatThrownBy(() ->
                userService.getUserById(INVALID_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(INVALID_ID);

        verify(userRepository).findById(INVALID_ID);

        verify(userMapper, never()).toResponse(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(ints = {3, 0})
    public void getAllUsers_givenMultipleUsers_shouldReturnUserList(
            int userCount
    ) {

        // ARRANGE

        List<User> users =
                IntStream.range(0, userCount)
                        .mapToObj(this::user)
                        .toList();

        when(userRepository.findAll())
                .thenReturn(users);

        // ACT

        List<UserResponse> responses = userService.getAllUsers();

        // ASSERT

        verify(userRepository).findAll();

        assertThat(responses).hasSize(userCount);

        for (int i = 0; i < userCount; ++i) {
            assertThat(responses.get(i))
                    .usingRecursiveComparison()
                    .isEqualTo(users.get(i));
        }

        for (User user : users) {
            verify(userMapper).toResponse(user);
        }
    }

    // ---

    @BeforeEach
    public void setUp() {
        lenient().when(passwordEncoder.encode(any(String.class)))
                .thenAnswer(invocation ->
                        encode(invocation.getArgument(0)));

        lenient().when(userMapper.toResponse(any(User.class)))
                .thenAnswer(invocation -> {
                    User user = invocation.getArgument(0);

                    return new UserResponse(
                            user.getId(),
                            user.getUsername()
                    );
                });
    }

    // ---

    private User user(int num) {
        return User.builder()
                .id(ID + num)
                .username(USERNAME + num)
                .password(encode(PASSWORD + num))
                .role(UserRole.USER)
                .build();
    }

    private User user() {
        return User.builder()
                .id(ID)
                .username(USERNAME)
                .password(encode(PASSWORD))
                .role(UserRole.USER)
                .build();
    }

    private CreateUserRequest createUserRequest(int num) {
        return new CreateUserRequest(
                USERNAME + num,
                PASSWORD + num
        );
    }

    private CreateUserRequest createUserRequest() {
        return new CreateUserRequest(
                USERNAME,
                PASSWORD
        );
    }

    private String encode(String password) {
        return ENCODED_PREFIX + password;
    }
}
