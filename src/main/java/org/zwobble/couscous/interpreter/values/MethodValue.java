package org.zwobble.couscous.interpreter.values;

import java.util.List;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Environment;

public final class MethodValue implements Callable {
    private final List<TypeName> argumentTypes;
    private final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply;
    
    public InterpreterValue apply(Environment environment, MethodCallArguments<InterpreterValue> arguments) {
        return apply.apply(environment, arguments);
    }
    
    public MethodValue(final List<TypeName> argumentTypes, final BiFunction<Environment, MethodCallArguments<InterpreterValue>, InterpreterValue> apply) {
        this.argumentTypes = argumentTypes;
        this.apply = apply;
    }
    
    public List<TypeName> getArgumentTypes() {
        return this.argumentTypes;
    }
    
    @java.lang.Override
    public boolean equals(@Nullable final java.lang.Object o) {
        if (o == this) return true;
        if (!(o instanceof MethodValue)) return false;
        final MethodValue other = (MethodValue)o;
        final java.lang.Object this$argumentTypes = this.getArgumentTypes();
        final java.lang.Object other$argumentTypes = other.getArgumentTypes();
        if (this$argumentTypes == null ? other$argumentTypes != null : !this$argumentTypes.equals(other$argumentTypes)) return false;
        final java.lang.Object this$apply = this.apply;
        final java.lang.Object other$apply = other.apply;
        if (this$apply == null ? other$apply != null : !this$apply.equals(other$apply)) return false;
        return true;
    }
    
    @java.lang.Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final java.lang.Object $argumentTypes = this.getArgumentTypes();
        result = result * PRIME + ($argumentTypes == null ? 43 : $argumentTypes.hashCode());
        final java.lang.Object $apply = this.apply;
        result = result * PRIME + ($apply == null ? 43 : $apply.hashCode());
        return result;
    }
    
    @java.lang.Override
    public java.lang.String toString() {
        return "MethodValue(argumentTypes=" + this.getArgumentTypes() + ", apply=" + this.apply + ")";
    }
}