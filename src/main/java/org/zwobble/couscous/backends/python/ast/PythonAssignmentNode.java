package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonAssignment")
public class PythonAssignmentNode implements PythonStatementNode {
    PythonVariableReferenceNode target;
    PythonExpressionNode value;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
