package com.example.testsupport.framework.api.exceptions;

public class HttpValidationException extends RuntimeException {
    public HttpValidationException(String message) {
        super(message);
    }
    public HttpValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
