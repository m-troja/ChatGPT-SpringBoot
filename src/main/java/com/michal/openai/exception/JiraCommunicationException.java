package com.michal.openai.exception;

public class JiraCommunicationException extends RuntimeException  {
    public JiraCommunicationException(String maxRetryAttemptsReached) {
        super(maxRetryAttemptsReached);
    }
    public JiraCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
