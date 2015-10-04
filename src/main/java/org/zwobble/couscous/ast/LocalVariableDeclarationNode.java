package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.values.TypeReference;

import static org.zwobble.couscous.ast.VariableDeclaration.var;

import lombok.Value;

@Value(staticConstructor="localVariableDeclaration")
public class LocalVariableDeclarationNode implements VariableNode, StatementNode {
    public static LocalVariableDeclarationNode localVariableDeclaration(
            int id,
            String name,
            TypeReference type,
            ExpressionNode initialValue) {
        return localVariableDeclaration(var(id, name, type), initialValue);
    }
    
    VariableDeclaration declaration;
    ExpressionNode initialValue;
    
    @Override
    public <T> T accept(StatementNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
