package com.michal.openai.exception;

public class TaskSystemException extends RuntimeException  {
    public TaskSystemException(String message) {
        super(message);
    }
}
