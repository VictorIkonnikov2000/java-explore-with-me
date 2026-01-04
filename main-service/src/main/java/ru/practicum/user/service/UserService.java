package ru.practicum.user.service;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import java.util.Collection;

public interface UserService {
    UserDto addUser(NewUserRequest request);

    Collection<UserDto> getAllUsers(Collection<Long> ids, int from, int size);

    void deleteUser(Long id);
}