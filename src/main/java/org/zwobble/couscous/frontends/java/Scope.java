package org.zwobble.couscous.frontends.java;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Scope {
    public static Scope create() {
        return new Scope(new HashMap<>(), new HashSet<>(), Identifier.TOP);
    }

    private final Map<String, VariableDeclaration> variablesByKey;
    private final Set<Identifier> identifiers;
    private final Identifier identifier;

    private Scope(Map<String, VariableDeclaration> variablesByKey, Set<Identifier> identifiers, Identifier identifier) {
        this.variablesByKey = variablesByKey;
        this.identifiers = identifiers;
        this.identifier = identifier;
    }

    public Scope enterClass(TypeName className) {
        return new Scope(variablesByKey, identifiers, identifier.extend("class#" + className.getQualifiedName()));
    }

    public Scope enterConstructor() {
        return new Scope(variablesByKey, identifiers, identifier.extend("constructor"));
    }

    public Scope enterMethod(String name) {
        // TODO: distinguish overloads
        return new Scope(variablesByKey, identifiers, identifier.extend("method#" + name));
    }

    public FormalArgumentNode formalArgument(String name, TypeName type) {
        VariableDeclaration declaration = generateVariable(name, type);
        return FormalArgumentNode.formalArg(declaration);
    }

    public FormalArgumentNode formalArgument(String key, String name, TypeName type) {
        VariableDeclaration declaration = generateVariable(key, name, type);
        return FormalArgumentNode.formalArg(declaration);
    }

    public LocalVariableDeclarationNode localVariable(
        String key,
        String name,
        TypeName type,
        ExpressionNode initialValue
    ) {
        VariableDeclaration declaration = generateVariable(key, name, type);
        return LocalVariableDeclarationNode.localVariableDeclaration(declaration, initialValue);
    }

    public LocalVariableDeclarationNode localVariable(
        String name,
        TypeName type,
        ExpressionNode initialValue
    ) {
        VariableDeclaration declaration = generateVariable(name, type);
        return LocalVariableDeclarationNode.localVariableDeclaration(declaration, initialValue);
    }

    public ExpressionNode reference(String key) {
        VariableDeclaration variable = variablesByKey.get(key);
        if (variable == null) {
            throw new IllegalArgumentException("variable not found: " + key);
        }
        return VariableReferenceNode.reference(variable);
    }

    private VariableDeclaration generateVariable(String key, String name, TypeName type) {
        if (variablesByKey.containsKey(key)) {
            throw new IllegalArgumentException(key + " is already mapped");
        }
        VariableDeclaration variable = generateVariable(name, type);
        variablesByKey.put(key, variable);
        return variable;
    }

    public VariableDeclaration generateVariable(String name, TypeName type) {
        Identifier identifier = generateIdentifier(name);
        return VariableDeclaration.var(identifier, name, type);
    }

    private Identifier generateIdentifier(String name) {
        Identifier identifier = this.identifier.extend(name);
        int index = 0;
        while (identifiers.contains(identifier)) {
            identifier = this.identifier.extend(name + "_" + index);
            index++;
        }
        identifiers.add(identifier);
        return identifier;
    }
}
