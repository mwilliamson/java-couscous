package org.zwobble.couscous.util.asm;

import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.matcher.ElementMatchers;

public class TypeDescriptions {
    private TypeDescriptions() {
    }

    public static FieldDescription findField(Implementation.Target target, String fieldName) {
        return findField(target.getInstrumentedType(), fieldName);
    }

    public static FieldDescription findField(TypeDescription typeDescription, String fieldName) {
        return typeDescription
            .getDeclaredFields()
            .filter(ElementMatchers.named(fieldName))
            .getOnly();
    }

    public static MethodDescription findMethod(Class<?> clazz, String name, Class<?>... arguments) {
        return findMethod(new TypeDescription.ForLoadedType(clazz), name, arguments);
    }

    public static MethodDescription findMethod(TypeDescription typeDescription, String name, Class<?>... arguments) {
        return typeDescription
            .getDeclaredMethods()
            .filter(ElementMatchers.named(name).and(ElementMatchers.takesArguments(arguments)))
            .getOnly();
    }

    public static MethodDescription findConstructor(Class<?> clazz, Class<?>... arguments) {
        return findConstructor(new TypeDescription.ForLoadedType(clazz), arguments);
    }

    public static MethodDescription findConstructor(TypeDescription typeDescription, Class<?>... arguments) {
        return typeDescription
            .getDeclaredMethods()
            .filter(ElementMatchers.isConstructor().and(ElementMatchers.takesArguments(arguments)))
            .getOnly();
    }
}
