package org.arkasha.jwtspringmaven.mapper.impl;

import org.arkasha.jwtspringmaven.dto.UserDto;
import org.arkasha.jwtspringmaven.entity.User;
import org.arkasha.jwtspringmaven.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDto toDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setEmail(user.getEmail());
        userDto.setPassword(user.getPassword());
        return userDto;
    }

    @Override
    public User toEntity(UserDto userDto) {
        System.out.println("Mapping userDto: " + userDto.getEmail());
        User user = new User();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setEmail(userDto.getEmail());
        user.setPassword(userDto.getPassword());
        return user;
    }
}
