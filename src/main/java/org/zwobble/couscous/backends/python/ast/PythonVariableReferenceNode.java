package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonVariableReference")
public class PythonVariableReferenceNode implements PythonExpressionNode {
    String name;

    @Override
    public <T> T accept(PythonNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
