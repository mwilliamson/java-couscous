package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import javax.annotation.Nullable;

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
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonModuleNode)) return false;
        final PythonModuleNode other = (PythonModuleNode)o;
        final java.lang.Object this$statements = this.getStatements();
        final java.lang.Object other$statements = other.getStatements();
        if (this$statements == null ? other$statements != null : !this$statements.equals(other$statements)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $statements = this.getStatements();
        result = result * PRIME + ($statements == null ? 43 : $statements.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonModuleNode(statements=" + this.getStatements() + ")";
    }
}