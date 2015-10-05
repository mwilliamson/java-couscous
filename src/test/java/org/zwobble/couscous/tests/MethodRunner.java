package org.zwobble.couscous.tests;

import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.values.InterpreterValue;

public interface MethodRunner {
    InterpreterValue runMethod(ClassNode classNode, String methodName, List<InterpreterValue> arguments);
}
