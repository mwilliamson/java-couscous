package org.zwobble.couscous.frontends.java;

public class ExpressionReadError extends RuntimeException {
    public ExpressionReadError(String message, Exception cause) {
        super(message, cause);
    }
}
