package org.zwobble.couscous.ast;

import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;

import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class MethodSignature {
    public static MethodSignature signature(String name, List<Type> arguments, Type returnType) {
        return new MethodSignature(name, arguments, returnType);
    }

    private final String name;
    private final List<Type> arguments;
    private final Type returnType;

    public MethodSignature(String name, List<Type> arguments, Type returnType) {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public List<Type> getArguments() {
        return arguments;
    }

    public Type getReturnType() {
        return returnType;
    }

    public MethodSignature generic() {
        return new MethodSignature(
            name,
            eagerMap(arguments, Types::generic),
            Types.generic(returnType));
    }

    @Override
    public String toString() {
        return "MethodSignature(" +
            "name='" + name + '\'' +
            ", arguments=" + arguments +
            ", returnType=" + returnType +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodSignature that = (MethodSignature) o;

        if (!name.equals(that.name)) return false;
        if (!arguments.equals(that.arguments)) return false;
        return returnType.equals(that.returnType);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + arguments.hashCode();
        result = 31 * result + returnType.hashCode();
        return result;
    }
}
