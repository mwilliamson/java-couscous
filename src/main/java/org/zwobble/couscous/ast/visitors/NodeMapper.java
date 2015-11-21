package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface NodeMapper<T> {
    T visit(LiteralNode literal);
    T visit(VariableReferenceNode variableReference);
    T visit(ThisReferenceNode reference);
    T visit(AssignmentNode assignment);
    T visit(TernaryConditionalNode ternaryConditional);
    T visit(MethodCallNode methodCall);
    T visit(StaticMethodCallNode staticMethodCall);
    T visit(ConstructorCallNode call);
    T visit(FieldAccessNode fieldAccess);

    T visit(ReturnNode returnNode);
    T visit(ExpressionStatementNode expressionStatement);
    T visit(LocalVariableDeclarationNode localVariableDeclaration);
    T visit(IfStatementNode ifStatement);
    T visit(WhileNode whileLoop);

    T visit(FormalArgumentNode formalArgumentNode);
    T visit(AnnotationNode annotation);
    T visit(MethodNode methodNode);
    T visit(ConstructorNode constructorNode);
    T visit(FieldDeclarationNode declaration);
    T visit(ClassNode classNode);
}