package org.zwobble.couscous.interpreter;

import java.util.Map;

import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.values.InterpreterValue;

import lombok.val;

public class Evaluator implements ExpressionNodeVisitor<InterpreterValue> {
    private final Map<Integer, InterpreterValue> stackFrame;

    public Evaluator(Map<Integer, InterpreterValue> stackFrame) {
        this.stackFrame = stackFrame;
    }
    
    public InterpreterValue eval(ExpressionNode expression) {
        return expression.accept(this);
    }

    @Override
    public InterpreterValue visit(LiteralNode literal) {
        return literal.getValue();
    }

    @Override
    public InterpreterValue visit(VariableReferenceNode variableReference) {
        return stackFrame.get(variableReference.getReferentId());
    }

    @Override
    public InterpreterValue visit(Assignment assignment) {
        val value = eval(assignment.getValue());
        stackFrame.put(assignment.getTarget().getReferentId(), value);
        return value;
    }
}
