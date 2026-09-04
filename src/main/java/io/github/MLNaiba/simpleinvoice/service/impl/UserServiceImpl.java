package io.github.MLNaiba.simpleinvoice.service.impl;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.domain.UserRole;
import io.github.MLNaiba.simpleinvoice.dto.CreateUserRequest;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;
import io.github.MLNaiba.simpleinvoice.exception.ResourceAlreadyExistsException;
import io.github.MLNaiba.simpleinvoice.exception.ResourceNotFoundException;
import io.github.MLNaiba.simpleinvoice.mapper.UserMapper;
import io.github.MLNaiba.simpleinvoice.repository.UserRepository;
import io.github.MLNaiba.simpleinvoice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info(
                "Creating user [{}]",
                request.username()
        );

        if (userRepository.existsByUsername(request.username())) {
            throw new ResourceAlreadyExistsException("User", request.username());
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.USER)
                .build();

        User savedUser;

        try {
            savedUser = userRepository.save(user);
        } catch (DuplicateKeyException exception) {
            throw new ResourceAlreadyExistsException("User", request.username());
        }

        log.info(
                "User [{}] successfully created",
                request.username()
        );

        return userMapper.toResponse(savedUser);
    }

    @Override
    public UserResponse getUserById(String id) {
        log.info(
                "Retrieving user with id [{}]",
                id
        );

        User user = findById(id);

        log.info(
                "User with id [{}] successfully retrieved",
                id
        );

        return userMapper.toResponse(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        log.info(
                "Retrieving all users"
        );

        List<User> users = userRepository.findAll();

        log.info(
                "All users successfully retrieved"
        );

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    // ---

    private User findById(String id)
            throws ResourceNotFoundException {
        return userRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }
}
