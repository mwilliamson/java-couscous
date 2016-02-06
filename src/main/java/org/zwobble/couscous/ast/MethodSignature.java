package org.zwobble.couscous.ast;

import java.util.List;

public class MethodSignature {
    private final String name;
    private final List<TypeName> arguments;
    private final TypeName returnType;

    public MethodSignature(String name, List<TypeName> arguments, TypeName returnType) {
        this.name = name;
        this.arguments = arguments;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public List<TypeName> getArguments() {
        return arguments;
    }

    public TypeName getReturnType() {
        return returnType;
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
