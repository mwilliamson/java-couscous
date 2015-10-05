package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonIntegerLiteral")
public class PythonIntegerLiteralNode implements PythonExpressionNode {
    int value;

    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
