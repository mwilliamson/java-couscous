package org.zwobble.couscous.backends.python.ast;

import java.util.List;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;
import com.google.common.collect.ImmutableList;

public final class PythonFunctionDefinitionNode implements PythonStatementNode {
    
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private final String name;
        private final ImmutableList.Builder<String> argumentNames;
        private final ImmutableList.Builder<PythonStatementNode> body;
        
        public Builder(String name) {
            this.name = name;
            this.argumentNames = ImmutableList.builder();
            this.body = ImmutableList.builder();
        }
        
        public Builder argument(String argumentName) {
            argumentNames.add(argumentName);
            return this;
        }
        
        public PythonFunctionDefinitionNode build() {
            return pythonFunctionDefinition(name, argumentNames.build(), new PythonBlock(body.build()));
        }
    }
    private final String name;
    private final List<String> argumentNames;
    private final PythonBlock body;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonFunctionDefinitionNode(final String name, final List<String> argumentNames, final PythonBlock body) {
        this.name = name;
        this.argumentNames = argumentNames;
        this.body = body;
    }
    
    public static PythonFunctionDefinitionNode pythonFunctionDefinition(final String name, final List<String> argumentNames, final PythonBlock body) {
        return new PythonFunctionDefinitionNode(name, argumentNames, body);
    }
    
    public String getName() {
        return this.name;
    }
    
    public List<String> getArgumentNames() {
        return this.argumentNames;
    }
    
    public PythonBlock getBody() {
        return this.body;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonFunctionDefinitionNode(name=" + this.getName() + ", argumentNames=" + this.getArgumentNames() + ", body=" + this.getBody() + ")";
    }
}