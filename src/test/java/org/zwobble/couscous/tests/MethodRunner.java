package org.zwobble.couscous.tests;

import java.util.List;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.values.PrimitiveValue;

public interface MethodRunner {
    PrimitiveValue runMethod(ClassNode classNode, String methodName, List<PrimitiveValue> arguments);
}
