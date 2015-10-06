package org.zwobble.couscous.interpreter.values;

import java.util.List;

import org.zwobble.couscous.ast.TypeName;

public interface Callable {
    List<TypeName> getArgumentTypes();
}
