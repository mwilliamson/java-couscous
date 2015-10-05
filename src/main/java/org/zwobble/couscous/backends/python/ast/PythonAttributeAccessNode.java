package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonAttributeAccess")
public class PythonAttributeAccessNode implements PythonExpressionNode {
    PythonExpressionNode left;
    String attributeName;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
