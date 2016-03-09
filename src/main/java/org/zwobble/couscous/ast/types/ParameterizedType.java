package org.zwobble.couscous.ast.types;

import java.util.List;

public class ParameterizedType implements Type {
    public static ParameterizedType parameterizedType(ScalarType rawType, List<Type> parameters) {
        return new ParameterizedType(rawType, parameters);
    }

    private final ScalarType rawType;
    private final List<Type> parameters;

    public ParameterizedType(ScalarType rawType, List<Type> parameters) {
        this.rawType = rawType;
        this.parameters = parameters;
    }

    public ScalarType getRawType() {
        return rawType;
    }

    public List<Type> getParameters() {
        return parameters;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "ParameterizedType(" +
            "rawType=" + rawType +
            ", parameters=" + parameters +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterizedType that = (ParameterizedType) o;

        if (!rawType.equals(that.rawType)) return false;
        return parameters.equals(that.parameters);

    }

    @Override
    public int hashCode() {
        int result = rawType.hashCode();
        result = 31 * result + parameters.hashCode();
        return result;
    }
}
