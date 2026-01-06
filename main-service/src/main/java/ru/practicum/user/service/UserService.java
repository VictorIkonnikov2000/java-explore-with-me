package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository repository;
    private final UserMapper userMapper;

    public UserDto addUser(NewUserRequest request) {
        if (request == null) {
            log.error("Попытка добавить пользователя с пустым телом запроса");
            throw new BadRequestException("Запрос на добавление нового пользователя не может быть null");
        }

        if (repository.existsByEmail(request.getEmail())) {
            log.warn("Конфликт: пользователь с email {} уже существует", request.getEmail());
            throw new ConflictException("Пользователь с email: " + request.getEmail() + " существует");
        }

        User user = repository.save(userMapper.toUser(request));
        log.info("Пользователь успешно сохранен: id={}, email={}", user.getId(), user.getEmail());
        return userMapper.toUserDto(user);
    }

    public void deleteUser(Long id) {
        if (!repository.existsById(id)) {
            log.warn("Попытка удаления: пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id: " + id + " в базе отсутствует");
        }
        repository.deleteById(id);
        log.info("Пользователь с id {} успешно удален", id);
    }

    @Transactional(readOnly = true)
    public Collection<UserDto> getAllUsers(Collection<Long> ids, int from, int size) {
        log.debug("Вызвана выгрузка пользователей: ids={}, offset={}, limit={}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        Collection<UserDto> result;
        if (ids != null && !ids.isEmpty()) {
            result = userMapper.toUserDtoList(repository.findByIdIn(ids, pageable).getContent());
        } else {
            result = userMapper.toUserDtoList(repository.findAll(pageable).getContent());
        }

        log.info("Выгружено {} пользователей", result.size());
        return result;
    }
}
