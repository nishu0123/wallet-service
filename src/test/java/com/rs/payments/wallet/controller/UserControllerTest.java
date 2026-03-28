package com.rs.payments.wallet.controller;

import com.rs.payments.wallet.dto.CreateUserRequest;
import com.rs.payments.wallet.model.User;
import com.rs.payments.wallet.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

// --- CORRECT IMPORTS START ---
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // Correct post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // Correct jsonPath
// --- CORRECT IMPORTS END ---


@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
    }
/*
    @Test
    @DisplayName("POST /users - Should return 201 Created on success")
    void shouldReturn201WhenUserIsCreated() throws Exception {
        // Given
        User savedUser = new User(UUID.randomUUID(), "testuser", "test@example.com", null);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // When & Then
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"testuser\", \"email\": \"test@example.com\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        verify(userService, times(1)).createUser(any(User.class));
    }
*/

    @Test
    @DisplayName("Should create user")
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com");

        User createdUser = new User(UUID.randomUUID(), "testuser", "test@example.com", null);
        when(userService.createUser(any(User.class))).thenReturn(createdUser);

        // When
        ResponseEntity<User> response = userController.createUser(request);

        // Then
        assertEquals(201, response.getStatusCode().value());
        assertEquals(createdUser, response.getBody());
        verify(userService, times(1)).createUser(any(User.class));
    }


}
