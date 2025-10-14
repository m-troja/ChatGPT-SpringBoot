package com.michal.openai.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
    	super("Login " + username + " was not found");
    }
}