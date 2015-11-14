package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface NodeVisitor {
    void visit(LiteralNode literal);
    void visit(VariableReferenceNode variableReference);
    void visit(ThisReferenceNode reference);
    void visit(AssignmentNode assignment);
    void visit(TernaryConditionalNode ternaryConditional);
    void visit(MethodCallNode methodCall);
    void visit(StaticMethodCallNode staticMethodCall);
    void visit(ConstructorCallNode call);
    void visit(FieldAccessNode fieldAccess);

    void visit(ReturnNode returnNode);
    void visit(ExpressionStatementNode expressionStatement);
    void visit(LocalVariableDeclarationNode localVariableDeclaration);
    void visit(IfStatementNode ifStatement);
    void visit(WhileNode whileLoop);
    
    void visit(ClassNode classNode);
    void visit(MethodNode methodNode);
    void visit(ConstructorNode constructorNode);
}
