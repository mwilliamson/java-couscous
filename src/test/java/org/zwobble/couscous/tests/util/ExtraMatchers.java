package org.zwobble.couscous.tests.util;

import org.hamcrest.Description;
import org.hamcrest.DiagnosingMatcher;
import org.hamcrest.Matcher;

public class ExtraMatchers {
    public static <T, U> Matcher<T> isInstance(Class<U> type, Matcher<? super U> downcastMatcher) {
        return new DiagnosingMatcher<T>() {
            @Override
            protected boolean matches(Object item, Description mismatch) {
                if (null == item) {
                    mismatch.appendText("null");
                    return false;
                }

                if (!type.isInstance(item)) {
                    mismatch.appendValue(item).appendText(" is a " + item.getClass().getName());
                    return false;
                }

                if (!downcastMatcher.matches(item)) {
                    downcastMatcher.describeMismatch(item, mismatch);
                    return false;
                }

                return true;
            }

            @Override
            public void describeTo(Description description) {
                description
                    .appendText(type.getSimpleName())
                    .appendText(" with ");
                downcastMatcher.describeTo(description);
            }
        };
    }
}
