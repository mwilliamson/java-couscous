package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonGetSlice")
public class PythonGetSliceNode implements PythonExpressionNode {
    PythonExpressionNode receiver;
    List<PythonExpressionNode> arguments;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
