package com.ufund.api.ufundapi.persistence;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ufund.api.ufundapi.model.User;
import com.ufund.api.ufundapi.model.User.AccountType;

@Tag("Persistence-tier")
public class UserFileDAOTest {
    UserFileDAO userFileDAO;
    User[] testUsers;
    ObjectMapper mockObjectMapper;

    @BeforeEach
    public void setupUserFileDAO() throws IOException {
        mockObjectMapper = mock(ObjectMapper.class);
        testUsers = new User[3];
        testUsers[0] = new User(99, "alice", "pw1", AccountType.User, 0, 0);
        testUsers[1] = new User(100, "bob", "pw2", AccountType.Manager, 0, 0);
        testUsers[2] = new User(101, "carol", "pw3", AccountType.User, 0 ,0);

        when(mockObjectMapper
                .readValue(new File("doesnt_matter.txt"), User[].class))
                .thenReturn(testUsers);
        userFileDAO = new UserFileDAO("doesnt_matter.txt", mockObjectMapper);
    }

    @Test
    public void testSaveException() throws IOException {
        doThrow(new IOException())
                .when(mockObjectMapper)
                .writeValue(any(File.class), any(User[].class));

        assertThrows(IOException.class,
                () -> userFileDAO.createUser(new User(0, "eve", "pw5", AccountType.User, 0, 0)),
                "IOException not thrown");
    }

    @Test
    public void testConstructorException() throws IOException {
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        doThrow(new IOException()).when(failingMapper).readValue(new File("doesnt_matter.txt"), User[].class);

        assertThrows(IOException.class,() -> new UserFileDAO("doesnt_matter.txt", failingMapper),"IOException not thrown");
    }

    @Test
    public void testGetUsers() throws IOException {
        User[] users = userFileDAO.getUsers();
        assertEquals(testUsers.length, users.length);
        for (int i = 0; i < testUsers.length; i++) {
            assertEquals(testUsers[i], users[i]);
        }
    }

    @Test
    public void testFindUsers() throws IOException {
        User[] users = userFileDAO.findUsers("bo");
        assertEquals(1, users.length);
        assertEquals(testUsers[1], users[0]);
    }

    @Test
    public void testGetUserFound() throws IOException {
        User user = userFileDAO.getUser(99);
        assertEquals(testUsers[0], user);
    }

    @Test
    public void testGetUserNotFound() throws IOException {
        assertNull(userFileDAO.getUser(98));
    }

    @Test
    public void testCreateUser() throws IOException {
        User created = assertDoesNotThrow(
                () -> userFileDAO.createUser(new User(0, "dave", "pw4", AccountType.User, 0, 0)),
                "Unexpected exception thrown");

        assertNotNull(created);
        assertEquals("dave", created.getUsername());
        assertNotNull(userFileDAO.getUser(created.getId()));
    }

    @Test
    public void testCreateUserDuplicateUsernameReturnsNull() throws IOException {
        User duplicate = new User(0, "ALICE", "pwx", AccountType.User, 0, 0);
        User result = userFileDAO.createUser(duplicate);
        assertNull(result);
    }

    @Test
    public void testUpdateUser() throws IOException {
        User updated = new User(99, "alice2", "pw9", AccountType.Manager, 0, 0);
        User result = assertDoesNotThrow(() -> userFileDAO.updateUser(updated),
                "Unexpected exception thrown");
        assertNotNull(result);
        assertEquals(updated, userFileDAO.getUser(99));
    }

    @Test
    public void testUpdateUserNotFoundReturnsNull() throws IOException {
        User result = userFileDAO.updateUser(new User(98, "x", "x", AccountType.User, 0, 0));
        assertNull(result);
    }

    @Test
    public void testDeleteUser() throws IOException {
        boolean result = assertDoesNotThrow(() -> userFileDAO.deleteUser(99),
                "Unexpected exception thrown");
        assertEquals(true, result);
        assertNull(userFileDAO.getUser(99));
    }

    @Test
    public void testDeleteUserNotFoundReturnsFalse() throws IOException {
        boolean result = userFileDAO.deleteUser(98);
        assertEquals(false, result);
    }
}
