package org.zwobble.couscous.interpreter;

import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.values.InterpreterValue;

import lombok.val;

public class Evaluator implements ExpressionNodeVisitor<InterpreterValue> {
    private final Environment environment;

    public Evaluator(Environment environment) {
        this.environment = environment;
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
        return environment.get(variableReference.getReferentId());
    }

    @Override
    public InterpreterValue visit(Assignment assignment) {
        val value = eval(assignment.getValue());
        environment.put(assignment.getTarget().getReferentId(), value);
        return value;
    }
}
