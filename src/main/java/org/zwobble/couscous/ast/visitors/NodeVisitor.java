package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ConstructorNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.ThisReferenceNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

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
    
    void visit(ClassNode classNode);
    void visit(MethodNode methodNode);
    void visit(ConstructorNode constructorNode);
}
