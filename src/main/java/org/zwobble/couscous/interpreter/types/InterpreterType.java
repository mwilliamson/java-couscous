package org.zwobble.couscous.interpreter.types;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodSignature;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.values.InterpreterValue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InterpreterType {
    TypeName getName();
    Set<TypeName> getSuperTypes();
    Optional<FieldDeclarationNode> getField(String fieldName);
    List<StatementNode> getStaticConstructor();
    InterpreterValue callConstructor(Environment environment, List<InterpreterValue> arguments);
    InterpreterValue callMethod(Environment environment, InterpreterValue value, MethodSignature signature, List<InterpreterValue> arguments);
    InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments);
}
