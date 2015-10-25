package org.zwobble.couscous.tests;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.zwobble.couscous.ast.AnnotationNode;
import org.zwobble.couscous.ast.AssignableExpressionNode;
import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ConstructorNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.FieldAccessNode;
import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.util.ExtraArrays;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.base.Joiner;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.zwobble.couscous.util.ExtraArrays.stream;

@RunWith(Parameterized.class)
public class ValueObjectTests {
    private final Class<?> clazz;

    public ValueObjectTests(Class<?> clazz) {
        this.clazz = clazz;
    }
    
    @Parameters(name="{0} is value object")
    public static Iterable<Object[]> valueObjectTypes() {
        return asList(new Object[][] {
            {AnnotationNode.class},
            {AssignmentNode.class},
            {ClassNode.class},
            {ConstructorCallNode.class},
            {ConstructorNode.class},
            {ExpressionStatementNode.class},
            {FieldAccessNode.class},
            {FieldDeclarationNode.class},
            {FormalArgumentNode.class},
            {LiteralNode.class},
            {LocalVariableDeclarationNode.class},
            {MethodCallNode.class},
            {ReturnNode.class},
            {VariableDeclaration.class}
        });
    }
    
    @Test
    public <T> void toStringIncludesAllFields() {
        GeneratedValue value = generateValue(clazz, ValueObjectTests::generateInstance);
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

    @Test
    public void equalityAndHashCodeIncludeAllFields() throws Exception {
        assertEqualsWithHashCode(generateInstance(clazz), generateInstance(clazz));
        
        generateInstancesWithSlightDifference(clazz)
            .forEach(instance -> {
                assertNotEqualsWithHashCode(
                    generateFirstInstance(clazz),
                    instance);
            });
        
        assertNotEqualsWithHashCode(
            generateFirstInstance(clazz),
            generateSecondInstance(clazz));
    }
    
    private static void assertEqualsWithHashCode(Object first, Object second) {
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
    
    private static void assertNotEqualsWithHashCode(Object first, Object second) {
        assertNotEquals(first, second);
        assertNotEquals(first.hashCode(), second.hashCode());
    }
    
    private static Stream<Object> generateInstancesWithSlightDifference(Class<?> clazz) {
        Class<?>[] fieldTypes = fieldTypes(clazz);
        Method constructor = findStaticConstructor(clazz);
        
        return IntStream.range(0, fieldTypes.length)
            .mapToObj(index -> {
                Object[] arguments = ExtraArrays.mapWithIndex(
                    fieldTypes,
                    (argumentIndex, fieldType) ->
                        argumentIndex == index ? generateSecondInstance(fieldType) : generateFirstInstance(fieldType))
                    .toArray(Object[]::new);
                try {
                    return constructor.invoke(null, arguments);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }
    
    private static Object generateFirstInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string 1]";
        } else if (type.equals(List.class)) {
            return asList("[list 1]");
        } else if (type.equals(PrimitiveValue.class)) {
            return generateFirstInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateFirstInstance(LiteralNode.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateFirstInstance(VariableReferenceNode.class);
        } else {
            return generateValue(type, ValueObjectTests::generateFirstInstance).instance;         
        }
    }
    
    private static Object generateSecondInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string 2]";
        } else if (type.equals(List.class)) {
            return asList("[list 2]");
        } else if (type.equals(PrimitiveValue.class)) {
            return generateSecondInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateSecondInstance(LiteralNode.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateSecondInstance(VariableReferenceNode.class);
        } else {
            return generateValue(type, ValueObjectTests::generateSecondInstance).instance;           
        }
    }
    
    private static Object generateInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string]";
        } else if (type.equals(List.class)) {
            return asList("[list]");
        } else if (type.equals(PrimitiveValue.class)) {
            return generateInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateInstance(LiteralNode.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateInstance(VariableReferenceNode.class);
        } else {
            return generateValue(type, ValueObjectTests::generateInstance).instance;
        }
    }
    
    private static GeneratedValue generateValue(Class<?> type, Function<Class<?>, Object> generate) {
        try {
            Class<?>[] fieldTypes = fieldTypes(type);
            Method constructor = findStaticConstructor(type);
            Object[] arguments = ExtraArrays.map(fieldTypes, fieldType -> generate.apply(fieldType))
                .toArray(Object[]::new);
            Object instance = constructor.invoke(null, arguments);
            return new GeneratedValue(instance, arguments);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Class<?>[] fieldTypes(Class<?> type) {
        return asList(type.getDeclaredFields())
            .stream()
            .filter(field -> !Modifier.isStatic(field.getModifiers()))
            .map(field -> field.getType())
            .toArray(Class<?>[]::new);
    }
    
    private static Method findStaticConstructor(Class<?> type) {
        Class<?>[] fieldTypes = fieldTypes(type);
        return stream(type.getDeclaredMethods())
            .filter(method -> isStaticConstructor(type, fieldTypes, method))
            .findAny()
            .orElseThrow(() -> new RuntimeException("Could not find static constructor for " + type));
    }
    
    private static boolean isStaticConstructor(Class<?> type, Class<?>[] fieldTypes, Method method) {
        return method.getReturnType().equals(type) &&
            asList(fieldTypes).equals(asList(method.getParameterTypes()));
    }

    private static class GeneratedValue {
        private final Object instance;
        private final Object[] arguments;
        
        public GeneratedValue(Object value, Object[] arguments) {
            this.instance = value;
            this.arguments = arguments;
        }
    }
}
