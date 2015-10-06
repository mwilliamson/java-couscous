package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;

import static org.zwobble.couscous.ast.VariableReferenceNode.reference;

import lombok.Value;

@Value
public class AssignmentNode implements ExpressionNode {
    public static StatementNode assignStatement(AssignableExpressionNode target, ExpressionNode value) {
        return new ExpressionStatementNode(assign(target, value));
    }
    
    public static AssignmentNode assign(AssignableExpressionNode target, ExpressionNode value) {
        return new AssignmentNode(target, value);
    }
    
    public static StatementNode assignStatement(VariableNode target, ExpressionNode value) {
        return new ExpressionStatementNode(assign(target, value));
    }
    
    public static AssignmentNode assign(VariableNode target, ExpressionNode value) {
        return new AssignmentNode(reference(target), value);
    }
    
    AssignableExpressionNode target;
    ExpressionNode value;
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public TypeName getType() {
        return value.getType();
    }
}
