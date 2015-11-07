package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ConstructorNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.IfStatementNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

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
    default void visit(ClassNode classNode) {
    }
    
    @Override
    default void visit(MethodNode methodNode) {
    }
    
    @Override
    default void visit(ConstructorNode constructorNode) {
    }
}
