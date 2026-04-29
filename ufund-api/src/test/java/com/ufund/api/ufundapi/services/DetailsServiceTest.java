package com.ufund.api.ufundapi.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import com.ufund.api.ufundapi.model.User;

@Tag("Service-tier")
class DetailsServiceTest {
    private DetailsService detailsService;
    private UserService mockUserService;

    @BeforeEach
    void setUp() {
        detailsService = new DetailsService();
        mockUserService = mock(UserService.class);
        ReflectionTestUtils.setField(detailsService, "userService", mockUserService);
    }

    @Test
    void loadUserByUsernameReturnsUserDetailsWhenUserExists() throws IOException {
        User user = new User(1, "alice", "pw123", User.AccountType.User, 0.0, 0.0);
        when(mockUserService.getUser("alice")).thenReturn(user);

        UserDetails details = detailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("pw123", details.getPassword());
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() throws IOException {
        when(mockUserService.getUser("ghost")).thenReturn(null);

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> detailsService.loadUserByUsername("ghost"));

        assertEquals("ghost", ex.getMessage());
    }

    @Test
    void loadUserByUsernameThrowsServerBusyWhenUserServiceIOException() throws IOException {
        doThrow(new IOException("disk")).when(mockUserService).getUser("alice");

        UsernameNotFoundException ex = assertThrows(
                UsernameNotFoundException.class,
                () -> detailsService.loadUserByUsername("alice"));

        assertEquals("Server is busy, try again later", ex.getMessage());
    }
}
