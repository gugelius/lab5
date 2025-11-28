package org.arkasha.jwtspringmaven.service;

import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.arkasha.jwtspringmaven.dto.RefreshTokenDto;
import org.arkasha.jwtspringmaven.dto.UserCredentialsDto;
import org.arkasha.jwtspringmaven.dto.UserDto;
import org.springframework.data.crossstore.ChangeSetPersister;

import javax.naming.AuthenticationException;

public interface UserService {
    JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException;
    JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception;
    UserDto getUserById(String id) throws ChangeSetPersister.NotFoundException;
    UserDto getUserByEmail(String email) throws Exception;
    String addUser(UserDto user);
}
