package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonIfStatementNode implements PythonStatementNode {
    public static PythonIfStatementNode pythonIfStatement(
            PythonExpressionNode condition,
            List<PythonStatementNode> trueBranch,
            List<PythonStatementNode> falseBranch) {
        return new PythonIfStatementNode(
            condition,
            new PythonBlock(trueBranch),
            new PythonBlock(falseBranch));
    }

    private final PythonExpressionNode condition;
    private final PythonBlock trueBranch;
    private final PythonBlock falseBranch;

    private PythonIfStatementNode(
            PythonExpressionNode condition,
            PythonBlock trueBranch,
            PythonBlock falseBranch) {
        this.condition = condition;
        this.trueBranch = trueBranch;
        this.falseBranch = falseBranch;
    }
    
    public PythonExpressionNode getCondition() {
        return condition;
    }
    
    public PythonBlock getTrueBranch() {
        return trueBranch;
    }
    
    public PythonBlock getFalseBranch() {
        return falseBranch;
    }

    @Override
    public String toString() {
        return "PythonIfStatementNode(condition=" + condition + ", trueBranch="
               + trueBranch + ", falseBranch=" + falseBranch + ")";
    }

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
