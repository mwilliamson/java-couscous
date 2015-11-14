package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import java.util.List;

public class PythonWhileNode implements PythonStatementNode {
    public static PythonWhileNode pythonWhile(
            PythonExpressionNode condition,
            List<PythonStatementNode> body) {
        return new PythonWhileNode(condition, new PythonBlock(body));
    }

    private final PythonExpressionNode condition;
    private final PythonBlock body;

    private PythonWhileNode(PythonExpressionNode condition, PythonBlock body) {
        this.condition = condition;
        this.body = body;
    }

    public PythonExpressionNode getCondition() {
        return condition;
    }

    public PythonBlock getBody() {
        return body;
    }

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
