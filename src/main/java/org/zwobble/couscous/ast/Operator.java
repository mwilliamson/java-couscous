package org.zwobble.couscous.ast;

public enum Operator {
    BOOLEAN_AND("&&", "and", true),
    BOOLEAN_OR("||", "or", true),
    BOOLEAN_NOT("!", "negate", true),

    ADD("+", "add", false),
    SUBTRACT("-", "subtract", false),
    MULTIPLY("*", "multiply", false),
    DIVIDE("/", "divide", false),
    MOD("%", "mod", false),

    EQUALS("==", "equals", true),
    NOT_EQUALS("!=", "notEquals", true),
    GREATER_THAN(">", "greaterThan", true),
    GREATER_THAN_OR_EQUAL(">=", "greaterThanOrEqual", true),
    LESS_THAN("<", "lessThan", true),
    LESS_THAN_OR_EQUAL("<=", "lessThanOrEqual", true);

    private final String symbol;
    private final String methodName;
    private final boolean isBoolean;

    Operator(String symbol, String methodName, boolean isBoolean) {
        this.symbol = symbol;
        this.methodName = methodName;
        this.isBoolean = isBoolean;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isBoolean() {
        return isBoolean;
    }
}
