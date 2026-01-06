package ru.practicum.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.service.UserService;

import java.util.Collection;

@Slf4j
@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
@Validated
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto create(@RequestBody @Valid NewUserRequest request) {
        log.info("Получен запрос POST /admin/users на создание пользователя: {}", request.getEmail());
        return userService.addUser(request);
    }

    @GetMapping
    public Collection<UserDto> getUsers(@RequestParam(required = false) Collection<Long> ids,
                                        @RequestParam(defaultValue = "0") @Min(0) Integer from,
                                        @RequestParam(defaultValue = "10") @Min(1) Integer size) {
        log.info("Получен запрос GET /admin/users: ids={}, from={}, size={}", ids, from, size);
        return userService.getAllUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("Получен запрос DELETE /admin/users/{} на удаление пользователя", userId);
        userService.deleteUser(userId);
    }
}
