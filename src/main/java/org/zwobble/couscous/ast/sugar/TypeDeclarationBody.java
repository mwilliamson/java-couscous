package org.zwobble.couscous.ast.sugar;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.util.ExtraLists;

import java.util.List;

public class TypeDeclarationBody {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final ImmutableList.Builder<FieldDeclarationNode> fields = ImmutableList.builder();
        private final ImmutableList.Builder<StatementNode> staticConstructor = ImmutableList.builder();
        private final ImmutableList.Builder<StatementNode> instanceInitializers = ImmutableList.builder();
        private ConstructorNode constructor = ConstructorNode.DEFAULT;
        private final ImmutableList.Builder<MethodNode> methods = ImmutableList.builder();

        private Builder() {
        }

        public void field(FieldDeclarationNode field) {
            fields.add(field);
        }

        public void addInitializer(boolean isStatic, List<StatementNode> statements) {
            (isStatic ? staticConstructor : instanceInitializers).addAll(statements);
        }

        public void addInitializer(StatementNode statement) {
            staticConstructor.add(statement);
        }

        public void constructor(ConstructorNode constructor) {
            this.constructor = constructor;
        }

        public void addMethod(MethodNode method) {
            this.methods.add(method);
        }

        public TypeDeclarationBody build() {
            return new TypeDeclarationBody(
                fields.build(),
                staticConstructor.build(),
                ConstructorNode.constructor(
                    constructor.getArguments(),
                    ExtraLists.concat(instanceInitializers.build(), constructor.getBody())),
                methods.build());
        }
    }

    private final List<FieldDeclarationNode> fields;
    private final List<StatementNode> staticConstructor;
    private final ConstructorNode constructor;
    private final List<MethodNode> methods;

    public TypeDeclarationBody(
        List<FieldDeclarationNode> fields,
        List<StatementNode> staticConstructor, ConstructorNode constructor,
        List<MethodNode> methods)
    {
        this.fields = fields;
        this.staticConstructor = staticConstructor;
        this.constructor = constructor;
        this.methods = methods;
    }

    public List<FieldDeclarationNode> getFields() {
        return fields;
    }

    public List<StatementNode> getStaticConstructor() {
        return staticConstructor;
    }

    public ConstructorNode getConstructor() {
        return constructor;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }
}
