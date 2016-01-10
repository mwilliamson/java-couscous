package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public class NodeMapperWithDefault<T> implements NodeMapper<T> {
    private final T defaultValue;

    public NodeMapperWithDefault(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public T visit(LiteralNode literal) {
        return defaultValue;
    }
    
    @Override
    public T visit(VariableReferenceNode variableReference) {
        return defaultValue;
    }
    
    @Override
    public T visit(ThisReferenceNode reference) {
        return defaultValue;
    }
    
    @Override
    public T visit(AssignmentNode assignment) {
        return defaultValue;
    }
    
    @Override
    public T visit(TernaryConditionalNode ternaryConditional) {
        return defaultValue;
    }
    
    @Override
    public T visit(MethodCallNode methodCall) {
        return defaultValue;
    }
    
    @Override
    public T visit(ConstructorCallNode call) {
        return defaultValue;
    }
    
    @Override
    public T visit(FieldAccessNode fieldAccess) {
        return defaultValue;
    }

    @Override
    public T visit(TypeCoercionNode typeCoercion) {
        return defaultValue;
    }

    @Override
    public T visit(CastNode cast) {
        return defaultValue;
    }

    @Override
    public T visit(InstanceReceiver receiver) {
        return defaultValue;
    }

    @Override
    public T visit(StaticReceiver receiver) {
        return defaultValue;
    }

    @Override
    public T visit(ReturnNode returnNode) {
        return defaultValue;
    }
    
    @Override
    public T visit(ExpressionStatementNode expressionStatement) {
        return defaultValue;
    }
    
    @Override
    public T visit(LocalVariableDeclarationNode localVariableDeclaration) {
        return defaultValue;
    }
    
    @Override
    public T visit(IfStatementNode ifStatement) {
        return defaultValue;
    }

    @Override
    public T visit(WhileNode whileLoop) {
        return defaultValue;
    }

    @Override
    public T visit(AnnotationNode annotation) {
        return defaultValue;
    }

    @Override
    public T visit(FieldDeclarationNode declaration) {
        return defaultValue;
    }

    @Override
    public T visit(FormalArgumentNode formalArgumentNode) {
        return defaultValue;
    }

    @Override
    public T visit(ClassNode classNode) {
        return defaultValue;
    }
    
    @Override
    public T visit(MethodNode methodNode) {
        return defaultValue;
    }
    
    @Override
    public T visit(ConstructorNode constructorNode) {
        return defaultValue;
    }
}
