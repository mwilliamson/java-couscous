package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonConditionalExpression")
public class PythonConditionalExpressionNode implements PythonExpressionNode {
    PythonExpressionNode condition;
    PythonExpressionNode trueValue;
    PythonExpressionNode falseValue;
    
    @Override
    public <T> T accept(PythonNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
