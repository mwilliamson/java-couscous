package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.function.Function;

public interface ExpressionNode extends Node {
    TypeName getType();
    <T> T accept(ExpressionNodeMapper<T> visitor);
    
    @Override
    default <T> T accept(NodeMapper<T> visitor) {
        return accept(new ExpressionNodeMapper<T>() {
            @Override
            public T visit(LiteralNode literal) {
                return visitor.visit(literal);
            }

            @Override
            public T visit(VariableReferenceNode variableReference) {
                return visitor.visit(variableReference);
            }

            @Override
            public T visit(ThisReferenceNode reference) {
                return visitor.visit(reference);
            }

            @Override
            public T visit(AssignmentNode assignment) {
                return visitor.visit(assignment);
            }

            @Override
            public T visit(TernaryConditionalNode ternaryConditional) {
                return visitor.visit(ternaryConditional);
            }

            @Override
            public T visit(MethodCallNode methodCall) {
                return visitor.visit(methodCall);
            }

            @Override
            public T visit(ConstructorCallNode call) {
                return visitor.visit(call);
            }

            @Override
            public T visit(FieldAccessNode fieldAccess) {
                return visitor.visit(fieldAccess);
            }

            @Override
            public T visit(TypeCoercionNode typeCoercion) {
                return visitor.visit(typeCoercion);
            }

            @Override
            public T visit(CastNode cast) {
                return visitor.visit(cast);
            }
        });
    }

    ExpressionNode transform(NodeTransformer transformer);

    default ExpressionNode replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return transform(new NodeTransformer() {
            @Override
            public ExpressionNode defaultTransformExpression(ExpressionNode value) {
                return replace.apply(value);
            }
        });
    }
}
