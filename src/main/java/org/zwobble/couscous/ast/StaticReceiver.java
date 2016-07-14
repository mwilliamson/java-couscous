package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.util.ExtraIterables;

public class StaticReceiver implements Receiver {
    public static Receiver staticReceiver(String type) {
        return new StaticReceiver(ScalarType.of(type));
    }

    public static Receiver staticReceiver(ScalarType type) {
        return new StaticReceiver(type);
    }

    private final ScalarType type;

    public StaticReceiver(ScalarType type) {
        this.type = type;
    }

    public ScalarType getType() {
        return type;
    }

    @Override
    public <T> T accept(Mapper<T> mapper) {
        return mapper.visit(type);
    }

    @Override
    public int type() {
        return NodeTypes.STATIC_RECEIVER;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.empty();
    }

    @Override
    public Receiver transform(NodeTransformer transformer) {
        return new StaticReceiver(transformer.transform(type));
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
