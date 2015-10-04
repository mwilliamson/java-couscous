package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.TypeReference;

import lombok.Value;

@Value(staticConstructor="localVariableDeclaration")
public class LocalVariableDeclarationNode implements VariableNode, StatementNode {
    int id;
    TypeReference type;
    String name;
    ExpressionNode initialValue;
    
    @Override
    public <T> T accept(StatementNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
