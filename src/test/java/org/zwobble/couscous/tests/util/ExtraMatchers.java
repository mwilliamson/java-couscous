package org.zwobble.couscous.tests.util;

import org.hamcrest.*;

import java.util.List;
import java.util.function.Function;

import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

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

    public static <T, U> Matcher<T> hasFeature(
        String name,
        Function<? super T, U> extract,
        Matcher<? super U> subMatcher) {
        return new FeatureMatcher<T, U>(subMatcher, name, name) {
            @Override
            protected U featureValueOf(T actual) {
                return extract.apply(actual);
            }
        };
    }

    public static <T> Matcher<Iterable<? extends T>> containsExactly(List<Matcher<? super T>> matchers) {
        if (matchers.isEmpty()) {
            return Matchers.emptyIterable();
        } else {
            return Matchers.contains(matchers);
        }
    }

    public static <T> Matcher<Iterable<? extends T>> containsExactElements(List<T> values) {
        return containsExactly(eagerMap(values, value -> equalTo(value)));
    }
}
