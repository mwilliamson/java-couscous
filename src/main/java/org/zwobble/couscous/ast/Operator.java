package org.zwobble.couscous.ast;

public enum Operator {
    ADD("add", false),
    SUBTRACT("subtract", false),
    MULTIPLY("multiply", false),
    DIVIDE("divide", false),
    MOD("mod", false),

    EQUALS("equals", true),
    GREATER_THAN("greaterThan", true),
    GREATER_THAN_OR_EQUAL("greaterThanOrEqual", true),
    LESS_THAN("lessThan", true),
    LESS_THAN_OR_EQUAL("lessThanOrEqual", true);

    private final String methodName;
    private final boolean isBoolean;

    Operator(String methodName, boolean isBoolean) {
        this.methodName = methodName;
        this.isBoolean = isBoolean;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isBoolean() {
        return isBoolean;
    }
}
