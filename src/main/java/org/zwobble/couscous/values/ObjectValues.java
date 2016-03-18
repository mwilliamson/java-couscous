package org.zwobble.couscous.values;

import org.zwobble.couscous.types.ScalarType;

public class ObjectValues {
    public static final ScalarType VOID = ScalarType.of("void");
    public static final ScalarType CLASS = ScalarType.of("java.lang.Class");
    public static final ScalarType OBJECT = ScalarType.of("java.lang.Object");
    public static final ScalarType BOXED_INT = ScalarType.of("java.lang.Integer");
    public static final ScalarType BOXED_BOOLEAN = ScalarType.of("java.lang.Boolean");
}
