package com.citron.javaintegrationsalesforce.util.exception;

public class CallApiException extends Exception {
    public CallApiException () {
        super();
    }

    public CallApiException(String message) {
        super(message);
    }

    public CallApiException(String message, Throwable cause) {
        super(message, cause);
    }

    public CallApiException(Throwable cause) {
        super(cause);
    }
}
