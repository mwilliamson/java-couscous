package org.zwobble.couscous.interpreter.values;

import java.util.List;

import org.zwobble.couscous.values.TypeReference;

public interface Callable {
    List<TypeReference> getArgumentTypes();
}
