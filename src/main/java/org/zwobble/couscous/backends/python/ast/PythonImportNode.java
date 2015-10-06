package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonImport")
public class PythonImportNode implements PythonStatementNode {
    String moduleName;
    List<PythonImportAliasNode> aliases;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
