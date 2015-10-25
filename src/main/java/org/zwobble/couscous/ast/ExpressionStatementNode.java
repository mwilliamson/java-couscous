package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

public class ExpressionStatementNode implements StatementNode {
    public static ExpressionStatementNode expressionStatement(ExpressionNode expression) {
        return new ExpressionStatementNode(expression);
    }
    
    private final ExpressionNode expression;

    private ExpressionStatementNode(ExpressionNode expression) {
        this.expression = expression;
    }
    
    public ExpressionNode getExpression() {
        return expression;
    }
    
    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ExpressionStatementNode(expression=" + expression + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((expression == null) ? 0 : expression.hashCode());
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
        ExpressionStatementNode other = (ExpressionStatementNode) obj;
        if (expression == null) {
            if (other.expression != null)
                return false;
        } else if (!expression.equals(other.expression))
            return false;
        return true;
    }
}
