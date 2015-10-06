package org.zwobble.couscous.tests.util;

import org.zwobble.couscous.util.Action;
import org.zwobble.couscous.util.Casts;

import lombok.SneakyThrows;

public class ExtraAsserts {
    @SneakyThrows
    public static <T extends Throwable> T assertThrows(Class<T> exceptionType, Action action) {
        try {
            action.run();
            throw new AssertionError("Expected exception");
        } catch (Exception exception) {
            return Casts.tryCast(exceptionType, exception)
                .orElseThrow(() -> exception);
        }
    }
}
