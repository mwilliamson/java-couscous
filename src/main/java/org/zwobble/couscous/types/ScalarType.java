package org.zwobble.couscous.types;

import com.google.common.base.Strings;

import java.util.Optional;

public class ScalarType implements Type {
    public static ScalarType of(String qualifiedName) {
        if (Strings.isNullOrEmpty(qualifiedName)) {
            throw new IllegalArgumentException("qualifiedName cannot be blank");
        }
        if (qualifiedName.contains("<")) {
            throw new IllegalArgumentException(qualifiedName + " contains <");
        }
        return new ScalarType(qualifiedName);
    }
    
    private final String qualifiedName;
    
    private ScalarType(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
    
    public String getQualifiedName() {
        return qualifiedName;
    }
    
    public String getSimpleName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
    }

    public Optional<String> getPackage() {
        int lastDot = qualifiedName.lastIndexOf(".");
        if (lastDot == -1) {
            return Optional.empty();
        } else {
            return Optional.of(qualifiedName.substring(0, lastDot));
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
}
