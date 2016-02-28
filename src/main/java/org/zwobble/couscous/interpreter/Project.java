package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.types.InterpreterType;

public interface Project {
    InterpreterType findClass(TypeName name);
}
