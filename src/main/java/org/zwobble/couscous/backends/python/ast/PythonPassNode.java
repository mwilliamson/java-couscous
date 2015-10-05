package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonPassNode implements PythonStatementNode {
    public static final PythonPassNode PASS = new PythonPassNode();
    
    private PythonPassNode() {
    }
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
