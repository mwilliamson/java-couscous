package org.zwobble.couscous;

import org.zwobble.couscous.ast.ClassName;
import org.zwobble.couscous.interpreter.values.ConcreteType;

public interface Project {
    ConcreteType<?> findClass(ClassName name);
}
