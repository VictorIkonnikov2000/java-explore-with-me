package ru.practicum.user.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto mapToUserDto(User user);

    UserShortDto mapToUserShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User mapToUser(NewUserRequest request);

    List<UserDto> toUserDtoList(List<User> user);
}

