package com.ufund.api.ufundapi.exceptions;

public class UsernameTakenException extends Exception {
    public UsernameTakenException(String message) {
        super("Username is taken: " + message);
    }
}