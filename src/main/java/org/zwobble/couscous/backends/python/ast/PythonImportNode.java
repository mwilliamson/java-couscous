package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import javax.annotation.Nullable;

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
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonImportNode)) return false;
        final PythonImportNode other = (PythonImportNode)o;
        final java.lang.Object this$moduleName = this.getModuleName();
        final java.lang.Object other$moduleName = other.getModuleName();
        if (this$moduleName == null ? other$moduleName != null : !this$moduleName.equals(other$moduleName)) return false;
        final java.lang.Object this$aliases = this.getAliases();
        final java.lang.Object other$aliases = other.getAliases();
        if (this$aliases == null ? other$aliases != null : !this$aliases.equals(other$aliases)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $moduleName = this.getModuleName();
        result = result * PRIME + ($moduleName == null ? 43 : $moduleName.hashCode());
        final java.lang.Object $aliases = this.getAliases();
        result = result * PRIME + ($aliases == null ? 43 : $aliases.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonImportNode(moduleName=" + this.getModuleName() + ", aliases=" + this.getAliases() + ")";
    }
}