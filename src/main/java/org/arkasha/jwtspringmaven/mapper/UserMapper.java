package org.arkasha.jwtspringmaven.mapper;

import org.arkasha.jwtspringmaven.dto.UserDto;
import org.arkasha.jwtspringmaven.entity.User;

public interface UserMapper {
    UserDto toDto(User user);
    User toEntity(UserDto userDto);
}