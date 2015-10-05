package org.zwobble.couscous.tests.util;

import org.zwobble.couscous.util.Action;

public class ExtraAsserts {
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
