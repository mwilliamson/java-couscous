package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import java.util.List;

public class PythonListNode implements PythonExpressionNode {
    public static PythonExpressionNode pythonList(List<PythonExpressionNode> elements) {
        return new PythonListNode(elements);
    }

    private final List<PythonExpressionNode> elements;

    public PythonListNode(List<PythonExpressionNode> elements) {
        this.elements = elements;
    }

    public List<PythonExpressionNode> getElements() {
        return elements;
    }

    @Override
    public int precedence() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
