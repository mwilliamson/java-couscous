package org.zwobble.couscous.types;

import com.google.common.base.Strings;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.zwobble.couscous.util.ExtraLists.*;

public class ScalarType implements Type {
    public static ScalarType topLevel(String qualifiedName) {
        if (Strings.isNullOrEmpty(qualifiedName)) {
            throw new IllegalArgumentException("qualifiedName cannot be blank");
        }
        if (qualifiedName.contains("<")) {
            throw new IllegalArgumentException(qualifiedName + " contains <");
        }
        int lastDot = qualifiedName.lastIndexOf('.');
        String className = qualifiedName.substring(lastDot + 1);
        Optional<String> packageName = lastDot == -1 ? Optional.empty() : Optional.of(qualifiedName.substring(0, lastDot));
        return new ScalarType(packageName, list(className));
    }

    public static ScalarType innerType(ScalarType outerType, String name) {
        return new ScalarType(outerType.packageName, append(outerType.typeNames, name));
    }

    private final Optional<String> packageName;
    private final List<String> typeNames;
    private final String qualifiedName;

    public ScalarType(Optional<String> packageName, List<String> typeNames) {
        this.packageName = packageName;
        this.typeNames = typeNames;
        this.qualifiedName = packageName.map(p -> p + ".").orElse("") + String.join(".", typeNames);
    }

    public Optional<String> getPackage() {
        return packageName;
    }

    public List<String> getTypeNames() {
        return typeNames;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getSimpleName() {
        return getLast(typeNames);
    }

    public Optional<Type> outerType() {
        if (typeNames.size() < 2) {
            return Optional.empty();
        } else {
            return Optional.of(new ScalarType(packageName, typeNames.subList(0, typeNames.size() - 1)));
        }
    }

    @Override
    public String toString() {
        return "ScalarType(qualifiedName=" + qualifiedName + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((qualifiedName == null) ? 0 : qualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ScalarType other = (ScalarType) obj;
        if (qualifiedName == null) {
            if (other.qualifiedName != null)
                return false;
        } else if (!qualifiedName.equals(other.qualifiedName))
            return false;
        return true;
    }

    @Override
    public <T> T accept(Visitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public Type transformSubTypes(Function<Type, Type> transform) {
        return this;
    }
}
