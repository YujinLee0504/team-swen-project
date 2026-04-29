package com.ufund.api.ufundapi.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;

import com.ufund.api.ufundapi.exceptions.UsernameTakenException;
import com.ufund.api.ufundapi.model.User;
import com.ufund.api.ufundapi.services.UserService;

@RestController
@RequestMapping("user")
public class UserController {
    private static final Logger LOG = Logger.getLogger(UserController.class.getName());
    private UserService userService;
    public UserController (UserService userService) {
        this.userService = userService;
    }
    @GetMapping("/details")
    public ResponseEntity<User> getLoggedInUser(Principal sUser) {
        try {
            if (sUser == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            User user = userService.getUser(sUser.getName());
            return new ResponseEntity<User>(user, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/worldmap")
    public ResponseEntity<User[]> getWorldMap() {
        try {
            User[] users = userService.getWorldMap();
            return new ResponseEntity<User[]>(users, HttpStatus.OK);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/create")
    public ResponseEntity<User> createUser(@RequestParam String username, @RequestParam String password, @RequestParam String accountType, @RequestParam double latitude, @RequestParam double longitude) {
        try {
            User r = userService.createUser(username, password, accountType, latitude, longitude);
            return new ResponseEntity<>(r, HttpStatus.OK);
        } catch (UsernameTakenException usr) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getLocalizedMessage());
            return new ResponseEntity<User>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
