package com.example.demo.service;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.utils.Role;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.springframework.data.jpa.domain.AbstractPersistable_.id;
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@exmple.com");
        testUser.setPassword("Admin123");
        testUser.setRole(Role.PATIENT);
    }
    @Test
    @DisplayName("Should load user by username successfully")
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        UserDetails result =  userService.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser",result.getUsername());
        Mockito.verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void createUser() {
    }

    @Test
    void findByUsername() {
    }

    @Test
    void findById() {
    }

    @Test
    void findByUsernameOptional() {
    }

    @Test
    void findByIdOptional() {
    }

    @Test
    void existsByUsername() {
    }

    @Test
    void saveUser() {
    }

    @Test
    void updatePassword() {
    }
}