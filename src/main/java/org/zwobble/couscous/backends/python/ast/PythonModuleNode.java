package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonModule")
public class PythonModuleNode implements PythonNode {
    List<PythonStatementNode> statements;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }

}
