package org.zwobble.couscous.interpreter.types;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.interpreter.Arguments;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.values.InterpreterValue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InterpreterType {
    Type getType();
    Set<Type> getSuperTypes();
    Optional<FieldDeclarationNode> getField(String fieldName);
    List<StatementNode> getStaticConstructor();
    void callConstructor(Environment environment, InterpreterValue thisValue, Arguments arguments);
    InterpreterValue callMethod(Environment environment, InterpreterValue value, MethodSignature signature, Arguments arguments);
    InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, Arguments arguments);
}
