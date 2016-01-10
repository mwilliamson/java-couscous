package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;

import java.util.function.Function;

public class StaticReceiver implements Receiver {
    public static Receiver staticReceiver(TypeName type) {
        return new StaticReceiver(type);
    }

    private final TypeName type;

    public StaticReceiver(TypeName type) {
        this.type = type;
    }

    public TypeName getType() {
        return type;
    }

    @Override
    public Receiver replaceExpressions(Function<ExpressionNode, ExpressionNode> replace) {
        return this;
    }

    @Override
    public <T> T accept(Mapper<T> mapper) {
        return mapper.visit(type);
    }

    @Override
    public <T> T accept(NodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "StaticReceiver(" +
            "type=" + type +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaticReceiver that = (StaticReceiver) o;

        return type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }
}
