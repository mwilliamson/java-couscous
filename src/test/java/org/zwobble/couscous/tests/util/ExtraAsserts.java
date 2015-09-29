package org.zwobble.couscous.tests.util;

public class ExtraAsserts {
    public interface Action {
        void run();
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Action action) {
        try {
            action.run();
            throw new AssertionError("Expected exception");
        } catch (Exception exception) {
            if (exceptionType.isInstance(exception)) {
                return (T)exception;
            } else {
                throw exception;
            }
        }
    }
}
