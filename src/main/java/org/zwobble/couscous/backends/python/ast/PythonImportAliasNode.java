package org.zwobble.couscous.backends.python.ast;

public final class PythonImportAliasNode {
    private final String name;

    private PythonImportAliasNode(final String name) {
        this.name = name;
    }

    public static PythonImportAliasNode pythonImportAlias(final String name) {
        return new PythonImportAliasNode(name);
    }

    public String getName() {
        return this.name;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonImportAliasNode)) return false;
        final PythonImportAliasNode other = (PythonImportAliasNode)o;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        return true;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "PythonImportAliasNode(name=" + this.getName() + ")";
    }
}