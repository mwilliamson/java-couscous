package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value(staticConstructor="pythonBooleanLiteral")
public class PythonBooleanLiteralNode implements PythonExpressionNode {
    @Getter(value = AccessLevel.NONE)
    boolean value;
    
    public boolean getValue() {
        return value;
    }
    
    @Override
    public <T> T accept(PythonNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
