package org.zwobble.couscous.backends.python.ast;

import java.util.List;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonModuleNode implements PythonNode {
    private final List<PythonStatementNode> statements;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonModuleNode(final List<PythonStatementNode> statements) {
        this.statements = statements;
    }
    
    public static PythonModuleNode pythonModule(final List<PythonStatementNode> statements) {
        return new PythonModuleNode(statements);
    }
    
    public List<PythonStatementNode> getStatements() {
        return this.statements;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonModuleNode(statements=" + this.getStatements() + ")";
    }
}