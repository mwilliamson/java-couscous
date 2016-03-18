package org.zwobble.couscous.interpreter.values;

import java.util.List;

import org.zwobble.couscous.types.Type;

public interface Callable {
    List<Type> getArgumentTypes();
}
