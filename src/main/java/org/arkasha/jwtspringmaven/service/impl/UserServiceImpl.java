package org.arkasha.jwtspringmaven.service.impl;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.arkasha.jwtspringmaven.dto.RefreshTokenDto;
import org.arkasha.jwtspringmaven.dto.UserCredentialsDto;
import org.arkasha.jwtspringmaven.dto.UserDto;
import org.arkasha.jwtspringmaven.entity.User;
import org.arkasha.jwtspringmaven.mapper.UserMapper;
import org.arkasha.jwtspringmaven.repository.UserRepository;
import org.arkasha.jwtspringmaven.security.jwt.JwtService;
import org.arkasha.jwtspringmaven.service.UserService;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public JwtAuthenticationDto signIn(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        User user = findByCredentials(userCredentialsDto);
        JwtAuthenticationDto authToken = jwtService.generateAuthToken(user.getEmail());
        user.setToken(authToken.getToken());
        user.setRefreshToken(authToken.getRefreshToken());
        userRepository.save(user);

        return authToken;
    }

    @Override
    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
        try {
            String refreshToken = refreshTokenDto.getRefreshToken();
            User user = findByRefreshToken(refreshToken);
            JwtAuthenticationDto jwtAuthenticationDto = jwtService.refreshBaseToken(user.getEmail(), refreshToken, user.getToken());
            user.setToken(jwtAuthenticationDto.getToken());
            user.setRefreshToken(jwtAuthenticationDto.getRefreshToken());
            userRepository.save(user);

            return jwtAuthenticationDto;
        }
        catch (Exception e) {
            throw new AuthenticationException("Invalid refresh token");
        }
    }

    @Override
    public UserDto getUserById(String id) throws ChangeSetPersister.NotFoundException {
        return userMapper.toDto(userRepository.findByUserId(UUID.fromString(id))
                .orElseThrow(ChangeSetPersister.NotFoundException::new));
    }

    @Override
    public UserDto getUserByEmail(String email) throws Exception {
        try {
            User user = findByEmail(email);

            if (jwtService.validateJwtToken(user.getToken())) {
                return userMapper.toDto(user);
            }
            else {
                throw new ChangeSetPersister.NotFoundException();
            }
        }
        catch (Exception e) {
            throw new Exception(e);
        }
    }

    @Override
    public String addUser(UserDto userDto) {
        System.out.println("Got userDto: " + userDto.getEmail());
        User user = userMapper.toEntity(userDto);
        System.out.println("New user: " + user);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        System.out.println("Password encoded");
        userRepository.save(user);
        return "User added: " + user.toString();
    }

    private User findByCredentials(UserCredentialsDto userCredentialsDto) throws AuthenticationException {
        Optional<User> optionalUser = userRepository.findByEmail(userCredentialsDto.getEmail());
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(userCredentialsDto.getPassword(), user.getPassword()))
            {
                return user;
            }
        }
        throw new AuthenticationException("Email or password is incorrect");
    }

    private User findByEmail(String email) throws Exception {
        return userRepository.findByEmail(email).orElseThrow(() ->
                new Exception(String.format("User with email % not found", email)));
    }

    private User findByRefreshToken(String refreshToken) throws Exception {
        return userRepository.findByRefreshToken(refreshToken).orElseThrow(() ->
                new Exception(String.format("User with refreshToken % not found", refreshToken)));
    }

    private User findByToken(String token) throws Exception {
        return userRepository.findByRefreshToken(token).orElseThrow(() ->
                new Exception(String.format("User with token % not found", token)));
    }
}


//    @Override
//    public JwtAuthenticationDto refreshToken(RefreshTokenDto refreshTokenDto) throws Exception {
//        String refreshToken = refreshTokenDto.getRefreshToken();
//        if(refreshToken != null && jwtService.validateJwtToken(refreshToken)) {
//            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
//            JwtAuthenticationDto jwtAuthenticationDto = jwtService.refreshBaseToken(user.getEmail());
//            user.setToken(jwtAuthenticationDto.getToken());
//            user.setRefreshToken(jwtAuthenticationDto.getRefreshToken());
//            userRepository.save(user);
//
//            return jwtService.refreshBaseToken(user.getEmail());
//        }
//        throw new AuthenticationException("Invalid refresh token");
//    }