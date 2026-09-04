package io.github.MLNaiba.simpleinvoice.controller;

import io.github.MLNaiba.simpleinvoice.dto.CreateUserRequest;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;
import io.github.MLNaiba.simpleinvoice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createUser(
            @Valid @RequestBody CreateUserRequest createUserRequest) {
        return userService.createUser(createUserRequest);
    }

    @GetMapping("/{id}")
    public UserResponse getUserById(
            @PathVariable String id) {
        return userService.getUserById(id);
    }

    @GetMapping
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}
