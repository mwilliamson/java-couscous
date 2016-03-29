package org.zwobble.couscous.tests.frontends.java;

import org.junit.Test;
import org.zwobble.couscous.frontends.java.JavaTypes;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.TypeParameter;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.types.BoundTypeParameter.boundTypeParameter;
import static org.zwobble.couscous.types.TypeParameter.typeParameter;

public class JavaTypesTests {
    private static final ScalarType INT_TYPE = ScalarType.of("int");

    @Test
    public void bindingTypeToItselfReturnsType() {
        assertEquals(
            INT_TYPE,
            JavaTypes.bind(INT_TYPE, INT_TYPE));
    }
    @Test
    public void bindingTypeToGenericTypeParameterReturnsBoundTypeParameter() {
        TypeParameter typeParameter = typeParameter(ScalarType.of("List"), "T");
        assertEquals(
            boundTypeParameter(typeParameter, INT_TYPE),
            JavaTypes.bind(typeParameter, INT_TYPE));
    }
}
