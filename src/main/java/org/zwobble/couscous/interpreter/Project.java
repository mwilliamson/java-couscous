package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.interpreter.types.InterpreterType;

public interface Project {
    InterpreterType findClass(ScalarType name);
}
