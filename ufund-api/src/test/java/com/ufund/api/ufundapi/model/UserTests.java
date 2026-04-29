package com.ufund.api.ufundapi.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class UserTests {
    @Test
    public void testConstructorAndGetters() {
        int id = 99;
        String username = "TestUser";
        String password = "Password123!"; // extremely secure
        User.AccountType type = User.AccountType.Manager;
        double lat = 42.069;
        double lon = -69.420;

        User user = new User(id, username, password, type, lat, lon);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(password, user.getPassword());
        assertEquals(type, user.getAccountType());
        assertEquals(lat, user.getLatitude());
        assertEquals(lon, user.getLongitude());
    }

    @Test
    public void testSetters() {
        User user = new User(1, "OldName", "OldPass", User.AccountType.User, 0.0, 0.0);

        String newName = "NewName";
        String newPass = "NewPass@123";
        User.AccountType newType = User.AccountType.Manager;
        double newLat = 10.5;
        double newLon = 20.5;

        user.setUsername(newName);
        user.setPassword(newPass);
        user.setAccountType(newType);
        user.setLatitude(newLat);
        user.setLongitude(newLon);

        assertEquals(newName, user.getUsername());
        assertEquals(newPass, user.getPassword());
        assertEquals(newType, user.getAccountType());
        assertEquals(newLat, user.getLatitude());
        assertEquals(newLon, user.getLongitude());
    }

    @Test
    public void testAccountTypeEnum() {
        assertEquals("User", User.AccountType.User.name());
        assertEquals("Manager", User.AccountType.Manager.name());
        assertEquals(2, User.AccountType.values().length);
    }

    @Test
    public void testUserCoordinatesAtZero() {
        User user = new User(5, "ZeroUser", "Pass", User.AccountType.User, 0.0, 0.0);

        assertEquals(0.0, user.getLatitude());
        assertEquals(0.0, user.getLongitude());
    }
}