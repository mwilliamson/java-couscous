package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.ast.ClassName;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonImport")
public class PythonImportNode implements PythonStatementNode {
    public static PythonImportNode pythonImport(String name) {
        return pythonImport(ClassName.of(name));
    }
    
    ClassName name;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
