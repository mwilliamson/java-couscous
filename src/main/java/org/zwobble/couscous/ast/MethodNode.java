package org.zwobble.couscous.ast;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.visitors.NodeMapper;

import java.util.List;

public class MethodNode implements CallableNode {
    public static Builder staticMethod(String name) {
        return builder(name).isStatic(true);
    }
    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static class Builder {
        private final ImmutableList.Builder<AnnotationNode> annotations =
            ImmutableList.builder();
        private boolean isStatic = false;
        private final String name;
        private final ImmutableList.Builder<FormalArgumentNode> arguments =
            ImmutableList.builder();
        private final ImmutableList.Builder<StatementNode> body =
            ImmutableList.builder();
        
        public Builder(String name) {
            this.name = name;
        }
        
        public Builder annotations(List<AnnotationNode> annotations) {
            this.annotations.addAll(annotations);
            return this;
        }
        
        public Builder isStatic(boolean isStatic) {
            this.isStatic = isStatic;
            return this;
        }
        
        public Builder argument(FormalArgumentNode argument) {
            arguments.add(argument);
            return this;
        }
        
        public Builder arguments(List<FormalArgumentNode> arguments) {
            this.arguments.addAll(arguments);
            return this;
        }
        
        public Builder statement(StatementNode statement) {
            this.body.add(statement);
            return this;
        }
        
        public Builder body(List<StatementNode> statements) {
            this.body.addAll(statements);
            return this;
        }
        
        public MethodNode build() {
            return new MethodNode(
                annotations.build(),
                isStatic,
                name,
                arguments.build(),
                body.build());
        }
    }
    
    public static MethodNode method(
            List<AnnotationNode> annotations,
            boolean isStatic,
            String name,
            List<FormalArgumentNode> arguments, 
            List<StatementNode> body) {
        return new MethodNode(annotations, isStatic, name, arguments, body);
    }
    
    private final List<AnnotationNode> annotations;
    private final boolean isStatic;
    private final String name;
    private final List<FormalArgumentNode> arguments; 
    private final List<StatementNode> body;
    
    private MethodNode(
            List<AnnotationNode> annotations,
            boolean isStatic,
            String name,
            List<FormalArgumentNode> arguments, 
            List<StatementNode> body) {
        this.annotations = annotations;
        this.isStatic = isStatic;
        this.name = name;
        this.arguments = arguments;
        this.body = body;
    }
    
    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }
    
    public boolean isStatic() {
        return isStatic;
    }
    
    public String getName() {
        return name;
    }
    
    public List<FormalArgumentNode> getArguments() {
        return arguments;
    }
    
    public List<StatementNode> getBody() {
        return body;
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }
    
    @Override
    public String toString() {
        return "MethodNode(annotations=" + annotations + ", isStatic="
               + isStatic + ", name=" + name + ", arguments=" + arguments
               + ", body=" + body + ")";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((annotations == null) ? 0 : annotations.hashCode());
        result = prime * result
                 + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result + ((body == null) ? 0 : body.hashCode());
        result = prime * result + (isStatic ? 1231 : 1237);
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodNode other = (MethodNode) obj;
        if (annotations == null) {
            if (other.annotations != null)
                return false;
        } else if (!annotations.equals(other.annotations))
            return false;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (body == null) {
            if (other.body != null)
                return false;
        } else if (!body.equals(other.body))
            return false;
        if (isStatic != other.isStatic)
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}
