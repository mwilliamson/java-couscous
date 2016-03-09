package org.zwobble.couscous.interpreter.values;

import java.util.List;

import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;

public interface Callable {
    List<Type> getArgumentTypes();
}
