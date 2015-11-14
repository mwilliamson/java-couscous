package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface NodeVisitorWithEmptyDefaults extends NodeVisitor {
    @Override
    default void visit(LiteralNode literal) {
    }
    
    @Override
    default void visit(VariableReferenceNode variableReference) {
    }
    
    @Override
    default void visit(ThisReferenceNode reference) {
    }
    
    @Override
    default void visit(AssignmentNode assignment) {
    }
    
    @Override
    default void visit(TernaryConditionalNode ternaryConditional) {
    }
    
    @Override
    default void visit(MethodCallNode methodCall) {
    }
    
    @Override
    default void visit(StaticMethodCallNode staticMethodCall) {
    }
    
    @Override
    default void visit(ConstructorCallNode call) {
    }
    
    @Override
    default void visit(FieldAccessNode fieldAccess) {
    }

    @Override
    default void visit(ReturnNode returnNode) {
    }
    
    @Override
    default void visit(ExpressionStatementNode expressionStatement) {
    }
    
    @Override
    default void visit(LocalVariableDeclarationNode localVariableDeclaration) {
    }
    
    @Override
    default void visit(IfStatementNode ifStatement) {
    }

    @Override
    default void visit(WhileNode whileLoop) {
    }
    
    @Override
    default void visit(ClassNode classNode) {
    }
    
    @Override
    default void visit(MethodNode methodNode) {
    }
    
    @Override
    default void visit(ConstructorNode constructorNode) {
    }
}
