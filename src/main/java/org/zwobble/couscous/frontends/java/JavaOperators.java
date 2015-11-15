package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.zwobble.couscous.ast.Operator;

public class JavaOperators {
    static Operator readOperator(PrefixExpression.Operator operator) {
        if (operator == PrefixExpression.Operator.INCREMENT) {
            return Operator.ADD;
        } else if (operator == PrefixExpression.Operator.DECREMENT) {
            return Operator.SUBTRACT;
        } else {
            throw new RuntimeException("Unrecognised operator: " + operator);
        }
    }

    static Operator readOperator(InfixExpression.Operator operator) {
        if (operator == InfixExpression.Operator.PLUS) {
            return Operator.ADD;
        } else if (operator == InfixExpression.Operator.MINUS) {
            return Operator.SUBTRACT;
        } else if (operator == InfixExpression.Operator.TIMES) {
            return Operator.MULTIPLY;
        } else if (operator == InfixExpression.Operator.DIVIDE) {
            return Operator.DIVIDE;
        } else if (operator == InfixExpression.Operator.REMAINDER) {
            return Operator.MOD;
        } else if (operator == InfixExpression.Operator.EQUALS) {
            return Operator.EQUALS;
        } else if (operator == InfixExpression.Operator.NOT_EQUALS) {
            return Operator.NOT_EQUALS;
        } else if (operator == InfixExpression.Operator.GREATER) {
            return Operator.GREATER_THAN;
        } else if (operator == InfixExpression.Operator.GREATER_EQUALS) {
            return Operator.GREATER_THAN_OR_EQUAL;
        } else if (operator == InfixExpression.Operator.LESS) {
            return Operator.LESS_THAN;
        } else if (operator == InfixExpression.Operator.LESS_EQUALS) {
            return Operator.LESS_THAN_OR_EQUAL;
        } else {
            throw new RuntimeException("Unrecognised operator: " + operator);
        }
    }

    static Operator readOperator(Assignment.Operator operator) {
        if (operator == Assignment.Operator.PLUS_ASSIGN) {
            return Operator.ADD;
        } else if (operator == Assignment.Operator.MINUS_ASSIGN) {
            return Operator.SUBTRACT;
        } else if (operator == Assignment.Operator.TIMES_ASSIGN) {
            return Operator.MULTIPLY;
        } else if (operator == Assignment.Operator.DIVIDE_ASSIGN) {
            return Operator.DIVIDE;
        } else if (operator == Assignment.Operator.REMAINDER_ASSIGN) {
            return Operator.MOD;
        } else {
            throw new RuntimeException("Unrecognised operator: " + operator);
        }
    }
}
