package io.github.MLNaiba.simpleinvoice.mapper;

import io.github.MLNaiba.simpleinvoice.domain.User;
import io.github.MLNaiba.simpleinvoice.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername()
        );
    }
}
