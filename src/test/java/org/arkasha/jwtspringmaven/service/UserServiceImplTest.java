package org.arkasha.jwtspringmaven.service;

import org.arkasha.jwtspringmaven.dto.JwtAuthenticationDto;
import org.arkasha.jwtspringmaven.dto.RefreshTokenDto;
import org.arkasha.jwtspringmaven.dto.UserCredentialsDto;
import org.arkasha.jwtspringmaven.dto.UserDto;
import org.arkasha.jwtspringmaven.entity.User;
import org.arkasha.jwtspringmaven.mapper.UserMapper;
import org.arkasha.jwtspringmaven.repository.UserRepository;
import org.arkasha.jwtspringmaven.security.jwt.JwtService;
import org.arkasha.jwtspringmaven.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.naming.AuthenticationException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void signIn_shouldReturnJwtToken_whenCredentialsAreValid() throws Exception {
        UserCredentialsDto credentials = new UserCredentialsDto("test@example.com", "password");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        JwtAuthenticationDto jwt = new JwtAuthenticationDto("access", "refresh");
        User savedUser = new User();
        savedUser.setEmail("test@example.com");
        savedUser.setToken("access");
        savedUser.setRefreshToken("refresh");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateAuthToken("test@example.com")).thenReturn(jwt);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        JwtAuthenticationDto result = userService.signIn(credentials);

        assertEquals("access", result.getToken());
        assertEquals("refresh", result.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signIn_shouldThrowAuthenticationException_whenPasswordIsInvalid() {
        UserCredentialsDto credentials = new UserCredentialsDto("test@example.com", "wrongPassword");
        User user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(AuthenticationException.class, () -> {
            userService.signIn(credentials);
        });
    }

    @Test
    void signIn_shouldThrowAuthenticationException_whenUserNotFound() {
        UserCredentialsDto credentials = new UserCredentialsDto("nonexistent@example.com", "password");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> {
            userService.signIn(credentials);
        });
    }

    @Test
    void refreshToken_shouldReturnNewToken_whenRefreshTokenIsValid() throws Exception {
        String refreshToken = "validToken";
        RefreshTokenDto dto = new RefreshTokenDto(refreshToken);
        User user = new User();
        user.setEmail("test@example.com");
        user.setToken("oldToken");
        user.setRefreshToken(refreshToken);

        JwtAuthenticationDto jwt = new JwtAuthenticationDto("newAccess", "newRefresh");

        when(userRepository.findByRefreshToken(refreshToken)).thenReturn(Optional.of(user));
        when(jwtService.refreshBaseToken("test@example.com", refreshToken, "oldToken")).thenReturn(jwt);
        when(userRepository.save(any(User.class))).thenReturn(user);

        JwtAuthenticationDto result = userService.refreshToken(dto);

        assertEquals("newAccess", result.getToken());
        assertEquals("newRefresh", result.getRefreshToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void refreshToken_shouldThrowAuthException_whenRefreshTokenNotFound() {
        RefreshTokenDto dto = new RefreshTokenDto("invalidToken");

        when(userRepository.findByRefreshToken("invalidToken")).thenReturn(Optional.empty());

        assertThrows(AuthenticationException.class, () -> {
            userService.refreshToken(dto);
        });
    }

    @Test
    void getUserByEmail_shouldReturnUserDto_whenUserExistsAndTokenIsValid() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setToken("validToken");

        UserDto userDto = new UserDto();
        userDto.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.validateJwtToken("validToken")).thenReturn(true);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserByEmail(email);

        assertEquals(email, result.getEmail());
    }

    @Test
    void getUserByEmail_shouldThrowException_whenUserNotFound() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> {
            userService.getUserByEmail(email);
        });
    }

    @Test
    void getUserByEmail_shouldThrowException_whenTokenIsInvalid() throws Exception {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setToken("invalidToken");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtService.validateJwtToken("invalidToken")).thenReturn(false);

        assertThrows(Exception.class, () -> {
            userService.getUserByEmail(email);
        });
    }

    @Test
    void getUserById_shouldReturnUserDto_whenUserExists() throws Exception {
        UUID userId = UUID.randomUUID();
        User user = new User();
        user.setUserId(userId);
        user.setEmail("test@example.com");

        UserDto userDto = new UserDto();
        userDto.setEmail("test@example.com");

        when(userRepository.findByUserId(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.getUserById(userId.toString());

        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getUserById_shouldThrowNotFoundException_whenUserDoesNotExist() {
        UUID userId = UUID.randomUUID();

        when(userRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ChangeSetPersister.NotFoundException.class, () -> {
            userService.getUserById(userId.toString());
        });
    }

    @Test
    void addUser_shouldReturnSuccessMessage() {
        UserDto userDto = new UserDto();
        userDto.setEmail("new@example.com");
        userDto.setPassword("plainPassword");

        User user = new User();
        user.setEmail("new@example.com");
        user.setPassword("plainPassword");

        User userWithEncodedPassword = new User();
        userWithEncodedPassword.setEmail("new@example.com");
        userWithEncodedPassword.setPassword("encodedPassword");

        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(userWithEncodedPassword);

        String result = userService.addUser(userDto);

        assertTrue(result.startsWith("User added: "));
        // assertTrue(result.startsWith("User created: "));
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("plainPassword");
    }
}