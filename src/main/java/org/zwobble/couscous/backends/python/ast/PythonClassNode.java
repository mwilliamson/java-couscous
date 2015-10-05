package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import com.google.common.collect.ImmutableList;

import lombok.Value;

@Value(staticConstructor="pythonClass")
public class PythonClassNode implements PythonStatementNode {
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    public static class Builder {
        private final String name;
        private final ImmutableList.Builder<PythonStatementNode> statements;

        public Builder(String name) {
            this.name = name;
            this.statements = ImmutableList.builder();
        }
        
        public PythonClassNode build() {
            return new PythonClassNode(name, new PythonBlock(statements.build()));
        }

        public Builder statement(PythonStatementNode statement) {
            statements.add(statement);
            return this;
        }
    }
    
    public static PythonClassNode pythonClass(String name, List<? extends PythonStatementNode> body) {
        return new PythonClassNode(name, new PythonBlock(body));
    }
    
    String name;
    PythonBlock body;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }

}
