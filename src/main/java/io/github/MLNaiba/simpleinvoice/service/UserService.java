package io.github.MLNaiba.simpleinvoice.service;

import io.github.MLNaiba.simpleinvoice.dto.CreateUserRequest;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(String id);

    List<UserResponse> getAllUsers();
}
