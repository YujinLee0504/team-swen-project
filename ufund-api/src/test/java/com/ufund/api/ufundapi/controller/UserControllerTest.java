package com.ufund.api.ufundapi.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.security.Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.ufund.api.ufundapi.exceptions.UsernameTakenException;
import com.ufund.api.ufundapi.model.User;
import com.ufund.api.ufundapi.services.UserService;

public class UserControllerTest {
    
    private UserService userService;
    private UserController userController;

    @BeforeEach
    void initializeTest() {
        this.userService = mock(UserService.class);
        this.userController = new UserController(userService);
    }

    // getLoggedInUser

    @Test
    void getLoggedInUserSuccess() throws IOException {
        Principal mockUser = Mockito.mock(Principal.class);
        Mockito.when(mockUser.getName()).thenReturn("Test123");
        User testUser = new User(100, "Test123", "password!", User.AccountType.User, 0, 0);
        when(userService.getUser("Test123")).thenReturn(testUser);
        ResponseEntity<User> result = userController.getLoggedInUser(mockUser);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(testUser, result.getBody());
    }

    @Test
    void getLoggedInUserFailure() throws IOException {
        // if there is no currently logged in user, the logged in user will be null, the endpoint requires authorization
        ResponseEntity<User> result = userController.getLoggedInUser(null);
        assertEquals(HttpStatus.UNAUTHORIZED, result.getStatusCode());
    }

    @Test
    void getLoggedInUserException() throws IOException {
        Principal mockUser = Mockito.mock(Principal.class);
        Mockito.when(mockUser.getName()).thenReturn("Test123");
        doThrow(new IOException()).when(userService).getUser("Test123");
        ResponseEntity<User> result = userController.getLoggedInUser(mockUser);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    // createUser

    @Test
    void createUserSuccess() throws IOException, UsernameTakenException {
        User testUser = new User(10, "Johnny", "abc123!", User.AccountType.User, 0, 0);
        when(userService.createUser("Johnny", "abc123!", "User", 0.0, 0.0)).thenReturn(testUser);
        ResponseEntity<User> result = userController.createUser("Johnny", "abc123!", "User", 0.0, 0.0);

        assertEquals(testUser, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}
