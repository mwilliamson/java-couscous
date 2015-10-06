package org.zwobble.couscous;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.values.ConcreteType;

public interface Project {
    ConcreteType<?> findClass(TypeName name);
}
