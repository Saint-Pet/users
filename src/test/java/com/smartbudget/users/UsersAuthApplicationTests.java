package com.smartbudget.users;

import com.smartbudget.users.model.User;
import com.smartbudget.users.repository.UserRepository;
import com.smartbudget.users.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class UsersAuthApplicationTests {


    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetUserById() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(1L);
        assertNotNull(foundUser);
        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void testCreateUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");

        when(passwordEncoder.encode(anyString())).thenReturn("encodedpassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User createdUser = userService.createUser(user);
        assertNotNull(createdUser);
        assertEquals("encodedpassword", createdUser.getPassword());
    }

    @Test
    void testUpdateUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("testuser");
        existingUser.setPassword("password");

        User updatedUser = new User();
        updatedUser.setUsername("updateduser");
        updatedUser.setPassword("newpassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodednewpassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(1L, updatedUser);
        assertNotNull(result);
        assertEquals("updateduser", result.getUsername());
        assertEquals("encodednewpassword", result.getPassword());
    }

    @Test
    void testDeleteUser() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

}
