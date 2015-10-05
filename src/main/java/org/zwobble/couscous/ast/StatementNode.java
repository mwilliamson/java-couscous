package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeVisitor;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

public interface StatementNode extends Node {
    <T> T accept(StatementNodeMapper<T> visitor);
    
    @Override
    default void accept(NodeVisitor visitor) {
        accept(new StatementNodeMapper<Void>() {
            @Override
            public Void visit(ReturnNode returnNode) {
                visitor.visit(returnNode);
                return null;
            }

            @Override
            public Void visit(ExpressionStatementNode expressionStatement) {
                visitor.visit(expressionStatement);
                return null;
            }

            @Override
            public Void visit(
                LocalVariableDeclarationNode localVariableDeclaration) {
                visitor.visit(localVariableDeclaration);
                return null;
            }
        });
    }
}
