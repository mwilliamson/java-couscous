package org.zwobble.couscous.ast;

import java.util.function.Supplier;

import org.zwobble.couscous.values.ConcreteType;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

@Value
public class FormalArgumentNode implements VariableNode {
    int id;
    @Getter(value=AccessLevel.NONE)
    Supplier<ConcreteType<?>> type;
    String name;
    
    public ConcreteType<?> getType() {
        return type.get();
    }
}
