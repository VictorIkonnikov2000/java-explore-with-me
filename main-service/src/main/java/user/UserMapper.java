package user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import user.dto.NewUserRequest;
import user.dto.UserDto;
import user.dto.UserShortDto;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDto mapToUserDto(User user);

    UserShortDto mapToUserShortDto(User user);

    @Mapping(target = "id", ignore = true)
    User mapToUser(NewUserRequest request);

    List<UserDto> toUserDtoList(List<User> user);
}

