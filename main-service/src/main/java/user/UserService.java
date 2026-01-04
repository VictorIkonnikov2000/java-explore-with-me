package user;

import user.dto.NewUserRequest;
import user.dto.UserDto;
import java.util.Collection;

public interface UserService {
    UserDto saveUser(NewUserRequest request);

    Collection<UserDto> getUsers(Collection<Long> ids, int from, int size);

    void deleteUser(Long id);
}
