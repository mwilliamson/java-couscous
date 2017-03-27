package org.zwobble.couscous.frontends.java;

public class ReadError extends RuntimeException {
    public ReadError(String message, Exception cause) {
        super(message, cause);
    }
}
