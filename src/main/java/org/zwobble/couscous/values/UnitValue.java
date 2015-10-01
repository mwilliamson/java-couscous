package org.zwobble.couscous.values;

import lombok.ToString;

@ToString
public class UnitValue implements InterpreterValue {
    private static final ConcreteType<?> TYPE = ConcreteType.<UnitValue>builder("Unit")
        .build();
    
    public static final UnitValue UNIT = new UnitValue(); 
    
    private UnitValue() {
    }

    @Override
    public ConcreteType<?> getType() {
        return TYPE;
    }
}
