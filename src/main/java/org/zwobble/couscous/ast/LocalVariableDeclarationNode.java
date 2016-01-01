package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

import java.util.function.Function;

import static org.zwobble.couscous.ast.VariableDeclaration.var;

public class LocalVariableDeclarationNode implements VariableNode, StatementNode {
    public static LocalVariableDeclarationNode localVariableDeclaration(
            Identifier id,
            String name,
            TypeName type,
            ExpressionNode initialValue) {
        return localVariableDeclaration(var(id, name, type), initialValue);
    }
    
    public static LocalVariableDeclarationNode localVariableDeclaration(
            VariableDeclaration declaration,
            ExpressionNode initialValue) {
        return new LocalVariableDeclarationNode(declaration, initialValue);
    }
    
    private final VariableDeclaration declaration;
    private final ExpressionNode initialValue;

    private LocalVariableDeclarationNode(
            VariableDeclaration declaration,
            ExpressionNode initialValue) {
        this.declaration = declaration;
        this.initialValue = initialValue;
    }
    
    public VariableDeclaration getDeclaration() {
        return declaration;
    }
    
    public ExpressionNode getInitialValue() {
        return initialValue;
    }
    
    public String getName() {
        return declaration.getName();
    }
    
    public TypeName getType() {
        return declaration.getType();
    }
    
    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public StatementNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return new LocalVariableDeclarationNode(declaration, replace.apply(initialValue));
    }

    @Override
    public String toString() {
        return "LocalVariableDeclarationNode(declaration=" + declaration
               + ", initialValue=" + initialValue + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((declaration == null) ? 0 : declaration.hashCode());
        result = prime * result
                 + ((initialValue == null) ? 0 : initialValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LocalVariableDeclarationNode other = (LocalVariableDeclarationNode) obj;
        if (declaration == null) {
            if (other.declaration != null)
                return false;
        } else if (!declaration.equals(other.declaration))
            return false;
        if (initialValue == null) {
            if (other.initialValue != null)
                return false;
        } else if (!initialValue.equals(other.initialValue))
            return false;
        return true;
    }
}
