package org.zwobble.couscous.values;

import java.util.List;

public interface Callable {
    List<ConcreteType<?>> getArgumentTypes();
}
