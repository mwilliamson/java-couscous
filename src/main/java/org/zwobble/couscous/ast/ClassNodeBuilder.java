package org.zwobble.couscous.ast;

import java.util.Optional;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

public class ClassNodeBuilder {
    private final TypeName name;
    private final ImmutableList.Builder<FieldDeclarationNode> fields;
    private Optional<ConstructorNode> constructor;
    private final ImmutableList.Builder<MethodNode> methods;
    
    public ClassNodeBuilder(String name) {
        this.name = TypeName.of(name);
        this.fields = ImmutableList.builder();
        this.constructor = Optional.empty();
        this.methods = ImmutableList.builder();
    }

    public ClassNodeBuilder field(String name, TypeName type) {
        this.fields.add(FieldDeclarationNode.field(name, type));
        return this;
    }
    
    public ClassNodeBuilder constructor(Function<ConstructorBuilder, ConstructorBuilder> build) {
        this.constructor = Optional.of(build.apply(new ConstructorBuilder()).build());
        return this;
    }
    
    public ClassNodeBuilder method(MethodNode method) {
        this.methods.add(method);
        return this;
    }
    
    public ClassNodeBuilder method(String name, Function<MethodBuilder, MethodBuilder> build) {
        this.methods.add(build.apply(new MethodBuilder(name)).build());
        return this;
    }
    
    public ClassNode build() {
        return new ClassNode(
            name,
            constructor.orElse(new ConstructorBuilder().build()),
            methods.build());
    }
    
    public class ConstructorBuilder {
        private final ImmutableList.Builder<FormalArgumentNode> arguments;
        private final ImmutableList.Builder<StatementNode> statements;
        
        public ConstructorBuilder() {
            this.arguments = ImmutableList.builder();
            this.statements = ImmutableList.builder();
        }
        
        public ThisReferenceNode thisReference() {
            return ThisReferenceNode.thisReference(name);
        }
        
        public ConstructorBuilder argument(FormalArgumentNode argument) {
            arguments.add(argument);
            return this;
        }
        
        public ConstructorBuilder statement(StatementNode statement) {
            statements.add(statement);
            return this;
        }
        
        public ConstructorNode build() {
            return ConstructorNode.constructor(
                arguments.build(),
                statements.build());
        }
    }
    
    public class MethodBuilder {
        private final String methodName;
        private final ImmutableList.Builder<FormalArgumentNode> arguments;
        private final ImmutableList.Builder<StatementNode> statements;

        public MethodBuilder(String name) {
            this.methodName = name;
            this.arguments = ImmutableList.builder();
            this.statements = ImmutableList.builder();
        }
        
        public ThisReferenceNode thisReference() {
            return ThisReferenceNode.thisReference(name);
        }
        
        public MethodBuilder argument(FormalArgumentNode argument) {
            arguments.add(argument);
            return this;
        }
        
        public MethodBuilder statement(StatementNode statement) {
            statements.add(statement);
            return this;
        }

        public MethodNode build() {
            return MethodNode.builder()
                .name(methodName)
                .arguments(arguments.build())
                .body(statements.build())
                .build();
        }
    }
}