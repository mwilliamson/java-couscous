package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonImport")
public class PythonImportNode implements PythonStatementNode {
    public static PythonImportNode pythonImport(String name) {
        return pythonImport(TypeName.of(name));
    }
    
    TypeName name;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
