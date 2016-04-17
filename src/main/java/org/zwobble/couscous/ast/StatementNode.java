package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

public interface StatementNode extends Node {
    <T> T accept(StatementNodeMapper<T> visitor);
    
    @Override
    default <T> T accept(NodeMapper<T> visitor) {
        return accept(new StatementNodeMapper<T>() {
            @Override
            public T visit(ReturnNode returnNode) {
                return visitor.visit(returnNode);
            }

            @Override
            public T visit(ThrowNode throwNode) {
                return visitor.visit(throwNode);
            }

            @Override
            public T visit(ExpressionStatementNode expressionStatement) {
                return visitor.visit(expressionStatement);
            }

            @Override
            public T visit(LocalVariableDeclarationNode localVariableDeclaration) {
                return visitor.visit(localVariableDeclaration);
            }

            @Override
            public T visit(IfStatementNode ifStatement) {
                return visitor.visit(ifStatement);
            }

            @Override
            public T visit(WhileNode whileLoop) {
                return visitor.visit(whileLoop);
            }
        });
    }

    StatementNode transform(NodeTransformer transformer);
}
