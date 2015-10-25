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
    public boolean equals(final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonFunctionDefinitionNode)) return false;
        final PythonFunctionDefinitionNode other = (PythonFunctionDefinitionNode)o;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$argumentNames = this.getArgumentNames();
        final java.lang.Object other$argumentNames = other.getArgumentNames();
        if (this$argumentNames == null ? other$argumentNames != null : !this$argumentNames.equals(other$argumentNames)) return false;
        final java.lang.Object this$body = this.getBody();
        final java.lang.Object other$body = other.getBody();
        if (this$body == null ? other$body != null : !this$body.equals(other$body)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $argumentNames = this.getArgumentNames();
        result = result * PRIME + ($argumentNames == null ? 43 : $argumentNames.hashCode());
        final java.lang.Object $body = this.getBody();
        result = result * PRIME + ($body == null ? 43 : $body.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonFunctionDefinitionNode(name=" + this.getName() + ", argumentNames=" + this.getArgumentNames() + ", body=" + this.getBody() + ")";
    }
}