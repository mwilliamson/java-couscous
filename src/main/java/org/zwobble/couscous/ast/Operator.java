package org.zwobble.couscous.ast;

public enum Operator {
    BOOLEAN_AND("&&", true),
    BOOLEAN_OR("||", true),
    BOOLEAN_NOT("!", true),

    ADD("+", false),
    SUBTRACT("-", false),
    MULTIPLY("*", false),
    DIVIDE("/", false),
    MOD("%", false),

    EQUALS("==", true),
    NOT_EQUALS("!=", true),
    GREATER_THAN(">", true),
    GREATER_THAN_OR_EQUAL(">=", true),
    LESS_THAN("<", true),
    LESS_THAN_OR_EQUAL("<=", true);

    private final String symbol;
    private final boolean isBoolean;

    Operator(String symbol, boolean isBoolean) {
        this.symbol = symbol;
        this.isBoolean = isBoolean;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isBoolean() {
        return isBoolean;
    }
}
