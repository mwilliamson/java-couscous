package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.interpreter.types.InterpreterType;

public interface Project {
    InterpreterType findClass(ScalarType name);
}
