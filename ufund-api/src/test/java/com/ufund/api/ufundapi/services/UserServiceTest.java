package com.ufund.api.ufundapi.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ufund.api.ufundapi.exceptions.UsernameTakenException;
import com.ufund.api.ufundapi.model.PledgeBasket;
import com.ufund.api.ufundapi.model.User;
import com.ufund.api.ufundapi.model.User.AccountType;
import com.ufund.api.ufundapi.persistence.PledgeBasketsDAO;
import com.ufund.api.ufundapi.persistence.UserDAO;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the UserService class
 */
@Tag("Service-tier")
public class UserServiceTest {

    private UserService userService;
    private UserDAO mockUserDao;
    private PledgeBasketsDAO mockPledgeBasketsDao;

    @BeforeEach
    public void setup() {
        mockUserDao = mock(UserDAO.class);
        mockPledgeBasketsDao = mock(PledgeBasketsDAO.class);
        userService = new UserService(mockUserDao, mockPledgeBasketsDao);
    }

    @Test
    public void testGetUserSuccess() throws IOException {
        String username = "testUser";
        User expectedUser = new User(1, username, "pass", AccountType.User, 0, 0);
        when(mockUserDao.findUsers(username)).thenReturn(new User[]{expectedUser});

        User actualUser = userService.getUser(username);

        assertNotNull(actualUser);
        assertEquals(username, actualUser.getUsername());
    }

    @Test
    public void testGetUserNotFound() throws IOException {
        when(mockUserDao.findUsers("nonexistent")).thenReturn(new User[0]);

        User actualUser = userService.getUser("nonexistent");

        assertNull(actualUser);
    }

    @Test
    public void testCreateUserSuccess() throws UsernameTakenException, IOException {
        String username = "JohnUser";
        User createdUser = new User(10, username, "password", AccountType.User, 43.0, -77.0);
        
        when(mockUserDao.findUsers(username)).thenReturn(new User[0]);
        when(mockUserDao.createUser(any(User.class))).thenReturn(createdUser);

        User result = userService.createUser(username, "password", "User", 43.0, -77.0);

        assertNotNull(result);
        assertEquals(username, result.getUsername());
        verify(mockPledgeBasketsDao, times(1)).addPledgeBasket(any(PledgeBasket.class));
    }

    @Test
    public void testCreateUserUsernameTaken() throws IOException {
        String username = "existingUser";
        User existingUser = new User(1, username, "pass", AccountType.User, 0, 0);
        when(mockUserDao.findUsers(username)).thenReturn(new User[]{existingUser});

        assertThrows(UsernameTakenException.class, () -> {
            userService.createUser(username, "password", "User", 0, 0);
        });
    }

    @Test
    public void testGetWorldMapFiltersUsersWithoutCoordinates() throws IOException {
        User userWithCoords = new User(1, "onMap", "p", AccountType.User, 10.5, 20.5);
        User userWithoutCoords = new User(2, "hidden", "p", AccountType.User, 0.0, 0.0);
        
        when(mockUserDao.findUsers("")).thenReturn(new User[]{userWithCoords, userWithoutCoords});

        User[] mapUsers = userService.getWorldMap();

        assertEquals(1, mapUsers.length);
        assertEquals("onMap", mapUsers[0].getUsername());
    }

    @Test
    public void testCreateUserManagerType() throws UsernameTakenException, IOException {
        String username = "admin";
        when(mockUserDao.findUsers(username)).thenReturn(new User[0]);
        when(mockUserDao.createUser(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        User result = userService.createUser(username, "pass", "Manager", 0, 0);

        assertEquals(AccountType.Manager, result.getAccountType());
    }
}