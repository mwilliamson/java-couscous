package org.zwobble.couscous.backends.python.ast;

import java.util.List;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public final class PythonImportNode implements PythonStatementNode {
    private final String moduleName;
    private final List<PythonImportAliasNode> aliases;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonImportNode(final String moduleName, final List<PythonImportAliasNode> aliases) {
        this.moduleName = moduleName;
        this.aliases = aliases;
    }
    
    public static PythonImportNode pythonImport(final String moduleName, final List<PythonImportAliasNode> aliases) {
        return new PythonImportNode(moduleName, aliases);
    }
    
    public String getModuleName() {
        return this.moduleName;
    }
    
    public List<PythonImportAliasNode> getAliases() {
        return this.aliases;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonImportNode(moduleName=" + this.getModuleName() + ", aliases=" + this.getAliases() + ")";
    }
}