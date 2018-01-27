package org.zwobble.couscous.ast;

public enum Operator {
    BOOLEAN_AND("&&", true, OperatorType.INFIX),
    BOOLEAN_OR("||", true, OperatorType.INFIX),
    BOOLEAN_NOT("!", true, OperatorType.PREFIX),

    INTEGER_NEGATION("-", false, OperatorType.PREFIX),
    ADD("+", false, OperatorType.INFIX),
    SUBTRACT("-", false, OperatorType.INFIX),
    MULTIPLY("*", false, OperatorType.INFIX),
    DIVIDE("/", false, OperatorType.INFIX),
    MOD("%", false, OperatorType.INFIX),

    EQUALS("==", true, OperatorType.INFIX),
    NOT_EQUALS("!=", true, OperatorType.INFIX),
    GREATER_THAN(">", true, OperatorType.INFIX),
    GREATER_THAN_OR_EQUAL(">=", true, OperatorType.INFIX),
    LESS_THAN("<", true, OperatorType.INFIX),
    LESS_THAN_OR_EQUAL("<=", true, OperatorType.INFIX);

    private final String symbol;
    private final boolean isBoolean;
    private final OperatorType type;

    Operator(String symbol, boolean isBoolean, OperatorType type) {
        this.symbol = symbol;
        this.isBoolean = isBoolean;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public OperatorType getType() {
        return type;
    }
}
