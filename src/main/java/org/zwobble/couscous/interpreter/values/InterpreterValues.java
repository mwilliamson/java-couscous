package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValueVisitor;
import org.zwobble.couscous.values.StringValue;
import org.zwobble.couscous.values.UnitValue;

public class InterpreterValues {
    public static final UnitInterpreterValue UNIT = UnitInterpreterValue.UNIT;
    public static final BooleanInterpreterValue FALSE = BooleanInterpreterValue.FALSE;
    public static final BooleanInterpreterValue TRUE = BooleanInterpreterValue.TRUE;

    public static InterpreterValue value(int value) {
        return new IntegerInterpreterValue(value);
    }
    
    public static InterpreterValue value(String value) {
        return StringInterpreterValue.of(value);
    }

    public static InterpreterValue value(PrimitiveValue value) {
        return value.accept(new PrimitiveValueVisitor<InterpreterValue>() {
            @Override
            public InterpreterValue visit(IntegerValue value) {
                return new IntegerInterpreterValue(value.getValue());
            }

            @Override
            public InterpreterValue visit(StringValue value) {
                return StringInterpreterValue.of(value.getValue());
            }

            @Override
            public InterpreterValue visit(BooleanValue value) {
                return new BooleanInterpreterValue(value.getValue());
            }

            @Override
            public InterpreterValue visit(UnitValue unitValue) {
                return UNIT;
            }
        });
    }
}
