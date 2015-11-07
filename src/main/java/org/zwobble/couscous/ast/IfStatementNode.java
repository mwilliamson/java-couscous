package org.zwobble.couscous.ast;

import java.util.List;

import org.zwobble.couscous.ast.visitors.StatementNodeMapper;

public class IfStatementNode implements StatementNode {
    public static IfStatementNode ifStatement(
            ExpressionNode condition,
            List<StatementNode> trueBranch,
            List<StatementNode> falseBranch) {
        return new IfStatementNode(condition, trueBranch, falseBranch);
    }
    
    private final ExpressionNode condition;
    private final List<StatementNode> trueBranch;
    private final List<StatementNode> falseBranch;

    private IfStatementNode(
            ExpressionNode condition,
            List<StatementNode> trueBranch,
            List<StatementNode> falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }

    public ExpressionNode getCondition() {
        return condition;
    }
    
    public List<StatementNode> getTrueBranch() {
        return trueBranch;
    }
    
    public List<StatementNode> getFalseBranch() {
        return falseBranch;
    }
    
    @Override
    public <T> T accept(StatementNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "IfStatementNode(condition=" + condition + ", trueBranch="
               + trueBranch + ", falseBranch=" + falseBranch + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((condition == null) ? 0 : condition.hashCode());
        result = prime * result
                 + ((falseBranch == null) ? 0 : falseBranch.hashCode());
        result = prime * result
                 + ((trueBranch == null) ? 0 : trueBranch.hashCode());
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
        IfStatementNode other = (IfStatementNode) obj;
        if (condition == null) {
            if (other.condition != null)
                return false;
        } else if (!condition.equals(other.condition))
            return false;
        if (falseBranch == null) {
            if (other.falseBranch != null)
                return false;
        } else if (!falseBranch.equals(other.falseBranch))
            return false;
        if (trueBranch == null) {
            if (other.trueBranch != null)
                return false;
        } else if (!trueBranch.equals(other.trueBranch))
            return false;
        return true;
    }
}
