package org.zwobble.couscous.interpreter.values;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValueVisitor;

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
            public InterpreterValue visitInteger(int value) {
                return new IntegerInterpreterValue(value);
            }

            @Override
            public InterpreterValue visitString(String value) {
                return StringInterpreterValue.of(value);
            }

            @Override
            public InterpreterValue visitBoolean(boolean value) {
                return BooleanInterpreterValue.of(value);
            }

            @Override
            public InterpreterValue visitUnit() {
                return UNIT;
            }

            @Override
            public InterpreterValue visitType(TypeName value) {
                return TypeInterpreterValue.of(value);
            }
        });
    }
}
