package com.example.testsupport.framework.api.exceptions;

public class HttpRequestFailedException extends RuntimeException {
    public HttpRequestFailedException(String message) {
        super(message);
    }
    public HttpRequestFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
