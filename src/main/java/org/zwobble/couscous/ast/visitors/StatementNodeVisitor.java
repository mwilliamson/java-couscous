package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.ReturnNode;

public interface StatementNodeVisitor<T> {
    T visit(ReturnNode returnNode);
    T visit(ExpressionStatementNode expressionStatement);
    T visit(LocalVariableDeclarationNode localVariableDeclaration);
}
