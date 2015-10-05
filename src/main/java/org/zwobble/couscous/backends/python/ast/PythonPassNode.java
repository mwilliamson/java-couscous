package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public class PythonPassNode implements PythonStatementNode {
    public static final PythonPassNode PASS = new PythonPassNode();
    
    private PythonPassNode() {
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public <T> T accept(PythonNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
