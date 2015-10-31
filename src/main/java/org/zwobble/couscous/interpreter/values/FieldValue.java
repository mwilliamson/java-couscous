package org.zwobble.couscous.interpreter.values;

import javax.annotation.Nullable;

import org.zwobble.couscous.ast.TypeName;

public final class FieldValue {
    private final String name;
    private final TypeName type;

    public FieldValue(final String name, final TypeName type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return this.name;
    }

    public TypeName getType() {
        return this.type;
    }

    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof FieldValue)) return false;
        final FieldValue other = (FieldValue)o;
        final java.lang.Object this$name = this.getName();
        final java.lang.Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final java.lang.Object this$type = this.getType();
        final java.lang.Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final java.lang.Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

    @java.lang.Override
    public java.lang.String toString() {
        return "FieldValue(name=" + this.getName() + ", type=" + this.getType() + ")";
    }
}