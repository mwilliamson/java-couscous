package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import lombok.Value;

@Value(staticConstructor="pythonFunctionDefinition")
public class PythonFunctionDefinitionNode implements PythonStatementNode {
    public static PythonFunctionDefinitionNode pythonFunctionDefinition(String name, List<PythonStatementNode> body) {
        return pythonFunctionDefinition(name, new PythonBlock(body));
    }
    
    String name;
    PythonBlock body;
    
    @Override
    public <T> T accept(PythonNodeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
