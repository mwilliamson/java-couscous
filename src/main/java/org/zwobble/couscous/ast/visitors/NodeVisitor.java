package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface NodeVisitor {
    void visit(LiteralNode literal);
    void visit(VariableReferenceNode variableReference);
    void visit(ThisReferenceNode reference);
    void visit(ArrayNode array);
    void visit(AssignmentNode assignment);
    void visit(TernaryConditionalNode ternaryConditional);
    void visit(MethodCallNode methodCall);
    void visit(ConstructorCallNode call);
    void visit(OperationNode operation);
    void visit(FieldAccessNode fieldAccess);
    void visit(TypeCoercionNode typeCoercion);
    void visit(CastNode cast);

    void visit(InstanceReceiver receiver);
    void visit(StaticReceiver receiver);

    void visit(ReturnNode returnNode);
    void visit(ExpressionStatementNode expressionStatement);
    void visit(LocalVariableDeclarationNode localVariableDeclaration);
    void visit(IfStatementNode ifStatement);
    void visit(WhileNode whileLoop);

    void visit(FormalTypeParameterNode parameter);
    void visit(FormalArgumentNode formalArgumentNode);
    void visit(AnnotationNode annotation);
    void visit(MethodNode methodNode);
    void visit(ConstructorNode constructorNode);
    void visit(FieldDeclarationNode declaration);
    void visit(ClassNode classNode);
    void visit(InterfaceNode interfaceNode);
}
