package org.zwobble.couscous.tests.util;

import org.zwobble.couscous.util.Action;
import org.zwobble.couscous.util.Casts;

public class ExtraAsserts {
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Action action) throws T {
        try {
            action.run();
            throw new AssertionError("Expected exception");
        } catch (RuntimeException exception) {
            return Casts.tryCast(exceptionType, exception)
                .orElseThrow(() -> exception);
        }
    }
}
