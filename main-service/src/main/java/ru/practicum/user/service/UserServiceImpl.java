package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.error.exceptions.BadRequestException;
import ru.practicum.error.exceptions.ConflictException;
import ru.practicum.error.exceptions.NotFoundException;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.mapper.UserMapper;
import ru.practicum.user.model.User;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

    @Override
    public UserDto addUser(NewUserRequest request) {

        if (request == null) {
            throw new BadRequestException("Запрос на добавление нового пользователя не может быть null");
        }

        isContainsEmail(request.getEmail());
        User user = userMapper.mapToUser(request);
        repository.save(user);
        return userMapper.mapToUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        isContainsUser(id);
        repository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers(Collection<Long> ids, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        Page<User> usersPage;

        if (ids != null) {
            usersPage = repository.findByIdIn(ids, pageable);
        } else {
            usersPage = repository.findAll(pageable);
        }

        List<User> userList = usersPage.getContent();
        return userMapper.toUserDtoList(userList);
    }

    private void isContainsEmail(String email) {
        boolean emailExists = repository.existsByEmail(email);

        if (emailExists) {
            throw new ConflictException("Пользователь с email: " + email + " существует");
        }
    }

    private void isContainsUser(Long id) {
        Optional<User> optUser = repository.findById(id);

        if (optUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id: " + id + " в базе отсутствует");
        }
    }
}