package org.zwobble.couscous.backends.python.ast;

import java.util.List;

import javax.annotation.Nullable;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;
import com.google.common.collect.ImmutableList;

public final class PythonClassNode implements PythonStatementNode {
    
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
    private final String name;
    private final PythonBlock body;
    
    @Override
    public void accept(PythonNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    private PythonClassNode(final String name, final PythonBlock body) {
        this.name = name;
        this.body = body;
    }
    
    public static PythonClassNode pythonClass(final String name, final PythonBlock body) {
        return new PythonClassNode(name, body);
    }
    
    public String getName() {
        return this.name;
    }
    
    public PythonBlock getBody() {
        return this.body;
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof PythonClassNode)) return false;
        final PythonClassNode other = (PythonClassNode)o;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
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
        final java.lang.Object $body = this.getBody();
        result = result * PRIME + ($body == null ? 43 : $body.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "PythonClassNode(name=" + this.getName() + ", body=" + this.getBody() + ")";
    }
}