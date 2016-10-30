package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.AnonymousType;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Iterables.concat;

public class AnonymousClass implements ExpressionNode {
    private final Optional<AnonymousType> anonymousType;
    private final Type superType;
    private final List<FieldDeclarationNode> fields;
    private final List<MethodNode> methods;

    public AnonymousClass(
        Optional<AnonymousType> anonymousType,
        Type superType,
        List<FieldDeclarationNode> fields,
        List<MethodNode> methods)
    {
        this.anonymousType = anonymousType;
        this.superType = superType;
        this.fields = fields;
        this.methods = methods;
    }

    @Override
    public Type getType() {
        return superType;
    }

    public Optional<AnonymousType> getAnonymousType() {
        return anonymousType;
    }

    @Override
    public int nodeType() {
        return NodeTypes.ANONYMOUS_CLASS;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return concat(
            fields,
            methods
        );
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new AnonymousClass(
            anonymousType,
            transformer.transform(superType),
            transformer.transformFields(fields),
            transformer.transformMethods(methods)
        );
    }

    public List<FieldDeclarationNode> getFields() {
        return fields;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }

    @Override
    public String toString() {
        return "AnonymousClass(" +
            "anonymousType=" + anonymousType +
            ", superType=" + superType +
            ", fields=" + fields +
            ", methods=" + methods +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AnonymousClass that = (AnonymousClass) o;

        if (!anonymousType.equals(that.anonymousType)) return false;
        if (!superType.equals(that.superType)) return false;
        if (!fields.equals(that.fields)) return false;
        return methods.equals(that.methods);

    }

    @Override
    public int hashCode() {
        int result = anonymousType.hashCode();
        result = 31 * result + superType.hashCode();
        result = 31 * result + fields.hashCode();
        result = 31 * result + methods.hashCode();
        return result;
    }
}
