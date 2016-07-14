package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

public class FieldDeclarationNode implements Node {
    public static FieldDeclarationNode field(String name, Type type) {
        return field(false, name, type);
    }

    public static FieldDeclarationNode staticField(String name, Type type) {
        return field(true, name, type);
    }

    public static FieldDeclarationNode field(boolean isStatic, String name, Type type) {
        return new FieldDeclarationNode(isStatic, name, type);
    }

    private final boolean isStatic;
    private final String name;
    private final Type type;
    
    private FieldDeclarationNode(boolean isStatic, String name, Type type) {
        this.isStatic = isStatic;
        this.name = name;
        this.type = type;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getName() {
        return name;
    }
    
    public Type getType() {
        return type;
    }

    @Override
    public int type() {
        return NodeTypes.FIELD_DECLARATION;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.empty();
    }

    public FieldDeclarationNode transform(NodeTransformer transformer) {
        return new FieldDeclarationNode(
            isStatic,
            transformer.transformFieldName(name),
            transformer.transform(type));
    }

    @Override
    public String toString() {
        return "FieldDeclarationNode(" +
            "isStatic=" + isStatic +
            ", name=" + name +
            ", type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FieldDeclarationNode that = (FieldDeclarationNode) o;

        if (isStatic != that.isStatic) return false;
        if (!name.equals(that.name)) return false;
        return type.equals(that.type);

    }

    @Override
    public int hashCode() {
        int result = (isStatic ? 1 : 0);
        result = 31 * result + name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }
}
