package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import com.google.common.collect.ImmutableList;

import lombok.Value;

@Value(staticConstructor="pythonFunctionDefinition")
public class PythonFunctionDefinitionNode implements PythonStatementNode {
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
    
    String name;
    List<String> argumentNames;
    PythonBlock body;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
}
