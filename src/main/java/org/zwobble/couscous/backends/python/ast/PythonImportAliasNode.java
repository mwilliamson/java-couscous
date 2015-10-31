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
    public java.lang.String toString() {
        return "PythonImportAliasNode(name=" + this.getName() + ")";
    }
}