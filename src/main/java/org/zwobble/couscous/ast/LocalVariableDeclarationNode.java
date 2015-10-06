package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import static org.zwobble.couscous.ast.VariableDeclaration.var;

import lombok.Value;

@Value(staticConstructor="localVariableDeclaration")
public class LocalVariableDeclarationNode implements VariableNode, StatementNode {
    public static LocalVariableDeclarationNode localVariableDeclaration(
            int id,
            String name,
            TypeName type,
            ExpressionNode initialValue) {
        return localVariableDeclaration(var(id, name, type), initialValue);
    }
    
    VariableDeclaration declaration;
    ExpressionNode initialValue;
    
    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }
}
