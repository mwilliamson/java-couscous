package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import static org.zwobble.couscous.ast.TypeCoercionNode.coerce;
import static org.zwobble.couscous.ast.VariableDeclaration.var;

public class LocalVariableDeclarationNode implements VariableNode, StatementNode {
    public static LocalVariableDeclarationNode localVariableDeclaration(
            Identifier id,
            String name,
            Type type,
            ExpressionNode initialValue) {
        return localVariableDeclaration(var(id, name, type), initialValue);
    }
    
    public static LocalVariableDeclarationNode localVariableDeclaration(
            VariableDeclaration declaration,
            ExpressionNode initialValue) {
        return new LocalVariableDeclarationNode(declaration, coerce(initialValue, declaration.getType()));
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
    
    public Type getType() {
        return declaration.getType();
    }
    
    @Override
    public int type() {
        return NodeTypes.LOCAL_VARIABLE_DECLARATION;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.of(initialValue);
    }

    @Override
    public StatementNode transformSubtree(NodeTransformer transformer) {
        return new LocalVariableDeclarationNode(
            transformer.transform(declaration),
            transformer.transformExpression(initialValue));
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
