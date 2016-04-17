package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.*;

public interface StatementNodeMapper<T> {
    T visit(ReturnNode returnNode);
    T visit(ThrowNode throwNode);
    T visit(ExpressionStatementNode expressionStatement);
    T visit(LocalVariableDeclarationNode localVariableDeclaration);
    T visit(IfStatementNode ifStatement);
    T visit(WhileNode whileLoop);
    T visit(TryNode tryStatement);
}
