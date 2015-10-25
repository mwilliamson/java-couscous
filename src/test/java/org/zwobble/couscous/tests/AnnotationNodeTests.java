package org.zwobble.couscous.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.stream.IntStream;

import org.junit.Test;
import org.zwobble.couscous.ast.AnnotationNode;
import org.zwobble.couscous.util.ExtraArrays;

import com.google.common.base.Joiner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.util.ExtraArrays.stream;

public class AnnotationNodeTests {
    @Test
    public void annotationNodeIsValueObject() throws Exception {
        assertIsValueObject(AnnotationNode.class);
    }

    private <T> void assertIsValueObject(Class<T> clazz) throws Exception {
        GeneratedValue value = generateValue(clazz);
        Field[] fields = clazz.getDeclaredFields();
        Object[] fieldStrings = IntStream.range(0, fields.length)
            .mapToObj(index -> String.format("%s=%s", fields[index].getName(), value.arguments[index]))
            .toArray();
        assertEquals(
            String.format("%s(%s)",
                clazz.getSimpleName(),
                Joiner.on(", ").join(fieldStrings)),
            value.instance.toString());
    }
    
    private GeneratedValue generateValue(Class<?> type) {
        try {
            Field[] fields = type.getDeclaredFields();
            Class<?>[] fieldTypes = ExtraArrays.map(fields, field -> field.getType())
                .toArray(Class<?>[]::new);
            Method constructor = findStaticConstructor(type, fieldTypes);
            Object[] arguments = ExtraArrays.map(fieldTypes, fieldType -> generateInstance(fieldType))
                .toArray(Object[]::new);
            Object instance = constructor.invoke(null, arguments);
            return new GeneratedValue(instance, arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private Object generateInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string]";
        } else {
            return generateValue(type).instance;            
        }
    }
    
    private Method findStaticConstructor(Class<?> type, Class<?>[] fieldTypes) {
        return stream(type.getDeclaredMethods())
            .filter(method -> isStaticConstructor(type, fieldTypes, method))
            .findAny()
            .orElseThrow(() -> new RuntimeException("Could not find static constructor for " + type));
    }
    
    private boolean isStaticConstructor(Class<?> type, Class<?>[] fieldTypes, Method method) {
        return method.getReturnType().equals(type) &&
            asList(fieldTypes).equals(asList(method.getParameterTypes()));
    }

    private class GeneratedValue {
        private final Object instance;
        private final Object[] arguments;
        
        public GeneratedValue(Object value, Object[] arguments) {
            this.instance = value;
            this.arguments = arguments;
        }
    }
}
