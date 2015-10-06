package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeVisitor;

public interface ExpressionNode extends Node {
    TypeName getType();
    <T> T accept(ExpressionNodeMapper<T> visitor);
    
    @Override
    default void accept(NodeVisitor visitor) {
        accept(new ExpressionNodeMapper<Void>() {
            @Override
            public Void visit(LiteralNode literal) {
                visitor.visit(literal);
                return null;
            }

            @Override
            public Void visit(VariableReferenceNode variableReference) {
                visitor.visit(variableReference);
                return null;
            }

            @Override
            public Void visit(AssignmentNode assignment) {
                visitor.visit(assignment);
                return null;
            }

            @Override
            public Void visit(TernaryConditionalNode ternaryConditional) {
                visitor.visit(ternaryConditional);
                return null;
            }

            @Override
            public Void visit(MethodCallNode methodCall) {
                visitor.visit(methodCall);
                return null;
            }

            @Override
            public Void visit(StaticMethodCallNode staticMethodCall) {
                visitor.visit(staticMethodCall);
                return null;
            }

            @Override
            public Void visit(ConstructorCallNode call) {
                visitor.visit(call);
                return null;
            }
        });
    }
}
