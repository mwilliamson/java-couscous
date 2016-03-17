package org.zwobble.couscous.backends.python.ast.visitors;

import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonStatementNode;

public class PythonExpressionStatement implements PythonStatementNode {
    public static PythonExpressionStatement pythonExpressionStatement(PythonExpressionNode expression) {
        return new PythonExpressionStatement(expression);
    }

    private final PythonExpressionNode expression;

    public PythonExpressionStatement(PythonExpressionNode expression) {
        this.expression = expression;
    }

    public PythonExpressionNode getExpression() {
        return expression;
    }

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
