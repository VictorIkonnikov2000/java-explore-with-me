package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.util.Collection;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

    public UserDto addUser(NewUserRequest request) {
        if (request == null) {
            throw new BadRequestException("Запрос на добавление нового пользователя не может быть null");
        }

        if (repository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Пользователь с email: " + request.getEmail() + " существует");
        }

        User user = repository.save(userMapper.toUser(request));
        return userMapper.toUserDto(user);
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Пользователь с id: " + id + " в базе отсутствует");
        }
        repository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers(Collection<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (ids != null && !ids.isEmpty()) {
            return userMapper.toUserDtoList(repository.findByIdIn(ids, pageable).getContent());
        } else {
            return userMapper.toUserDtoList(repository.findAll(pageable).getContent());
        }
    }
}
