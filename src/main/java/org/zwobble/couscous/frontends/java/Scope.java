package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.NaturalNumbers;

import java.util.*;

public class Scope {
    public static Scope create() {
        return new Scope(
            new HashMap<>(),
            new HashSet<>(),
            Identifier.TOP,
            "_couscous_tmp_",
            NaturalNumbers.INSTANCE.iterator()
        );
    }

    private final Map<String, VariableDeclaration> variablesByKey;
    private final Set<Identifier> identifiers;
    private final Identifier identifier;
    private final String temporaryPrefix;
    private final Iterator<Integer> temporaryCounter;

    private Scope(
        Map<String, VariableDeclaration> variablesByKey,
        Set<Identifier> identifiers,
        Identifier identifier,
        String temporaryPrefix,
        Iterator<Integer> temporaryCounter)
    {
        this.variablesByKey = variablesByKey;
        this.identifiers = identifiers;
        this.identifier = identifier;
        this.temporaryPrefix = temporaryPrefix;
        this.temporaryCounter = temporaryCounter;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Scope temporaryPrefix(String temporaryPrefix) {
        return new Scope(variablesByKey, identifiers, identifier, temporaryPrefix, temporaryCounter);
    }

    public Scope enterClass(ScalarType className) {
        return enter(identifier.type(className.getQualifiedName()));
    }

    public Scope enterConstructor() {
        return enter(identifier.constructor());
    }

    public Scope enterMethod(String name) {
        // TODO: distinguish overloads
        return enter(identifier.method(name));
    }

    private Scope enter(Identifier newIdentifier) {
        return new Scope(variablesByKey, identifiers, newIdentifier, temporaryPrefix, temporaryCounter);
    }

    public FormalArgumentNode formalArgument(String name, Type type) {
        VariableDeclaration declaration = generateVariable(name, type);
        return FormalArgumentNode.formalArg(declaration);
    }

    public FormalArgumentNode formalArgument(String key, String name, Type type) {
        VariableDeclaration declaration = generateVariable(key, name, type);
        return FormalArgumentNode.formalArg(declaration);
    }

    public LocalVariableDeclarationNode localVariable(
        String key,
        String name,
        Type type,
        ExpressionNode initialValue
    ) {
        VariableDeclaration declaration = generateVariable(key, name, type);
        return LocalVariableDeclarationNode.localVariableDeclaration(declaration, initialValue);
    }

    public LocalVariableDeclarationNode localVariable(
        String name,
        Type type,
        ExpressionNode initialValue
    ) {
        VariableDeclaration declaration = generateVariable(name, type);
        return LocalVariableDeclarationNode.localVariableDeclaration(declaration, initialValue);
    }

    public LocalVariableDeclarationNode temporaryVariable(ExpressionNode initialValue) {
        return temporaryVariable(initialValue.getType(), initialValue);
    }

    public LocalVariableDeclarationNode temporaryVariable(Type type, ExpressionNode initialValue) {
        return localVariable(temporaryPrefix + temporaryCounter.next(), type, initialValue);
    }

    public ExpressionNode reference(String key) {
        VariableDeclaration variable = variablesByKey.get(key);
        if (variable == null) {
            throw new IllegalArgumentException("variable not found: " + key);
        }
        return VariableReferenceNode.reference(variable);
    }

    public VariableDeclaration generateVariable(String key, String name, Type type) {
        if (variablesByKey.containsKey(key)) {
            throw new IllegalArgumentException(key + " is already mapped");
        }
        VariableDeclaration variable = generateVariable(name, type);
        variablesByKey.put(key, variable);
        return variable;
    }

    public VariableDeclaration generateVariable(String name, Type type) {
        Identifier identifier = generateVariableIdentifier(name);
        return VariableDeclaration.var(identifier, name, type);
    }

    private Identifier generateVariableIdentifier(String name) {
        Identifier identifier = this.identifier.variable(name);
        int index = 0;
        while (identifiers.contains(identifier)) {
            identifier = this.identifier.variable(name + "_" + index);
            index++;
        }
        identifiers.add(identifier);
        return identifier;
    }
}
