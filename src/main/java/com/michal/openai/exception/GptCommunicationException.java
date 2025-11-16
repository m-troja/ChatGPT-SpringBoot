package com.michal.openai.exception;

public class GptCommunicationException extends RuntimeException  {
    public GptCommunicationException(String message) {
        super(message);
    }
    public GptCommunicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
