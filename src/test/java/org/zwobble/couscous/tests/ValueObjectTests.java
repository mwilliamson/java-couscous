package org.zwobble.couscous.tests;

import com.google.common.base.Joiner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.types.*;
import org.zwobble.couscous.util.ExtraArrays;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.zwobble.couscous.util.ExtraArrays.stream;
import static org.zwobble.couscous.util.ExtraLists.eagerFilter;
import static org.zwobble.couscous.util.ExtraLists.list;

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
            {ArrayNode.class},
            {AssignmentNode.class},
            {BoundTypeParameter.class},
            {CastNode.class},
            {ClassNode.class},
            {ConstructorCallNode.class},
            {ConstructorNode.class},
            {ExpressionStatementNode.class},
            {FieldAccessNode.class},
            {FieldDeclarationNode.class},
            {FormalArgumentNode.class},
            {FormalTypeParameterNode.class},
            {IfStatementNode.class},
            {InstanceReceiver.class},
            {InterfaceNode.class},
            {LiteralNode.class},
            {LocalVariableDeclarationNode.class},
            {MethodCallNode.class},
            {MethodNode.class},
            {OperationNode.class},
            {ParameterizedType.class},
            {ReturnNode.class},
            {ScalarType.class},
            {StaticReceiver.class},
            {TernaryConditionalNode.class},
            {ThisReferenceNode.class},
            {TypeCoercionNode.class},
            {TypeParameter.class},
            {ScalarType.class},
            {VariableDeclaration.class},
            {VariableReferenceNode.class},
            {WhileNode.class}
        });
    }
    
    @Test
    public <T> void toStringIncludesAllFields() {
        GeneratedValue value = generateValue(clazz, ValueObjectTests::generateInstance);
        List<Field> fields = eagerFilter(asList(clazz.getDeclaredFields()), field -> !Modifier.isStatic(field.getModifiers()));
        Object[] fieldStrings = IntStream.range(0, fields.size())
            .mapToObj(index -> String.format("%s=%s", fields.get(index).getName(), value.arguments[index]))
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
        } else if (type.equals(boolean.class)) {
            return true;
        } else if (type.equals(Optional.class)) {
            return Optional.of("[optional 1]");
        } else if (type.equals(List.class)) {
            return list("[list 1]");
        } else if (type.equals(Set.class)) {
            return Collections.singleton("[set 1]");
        } else if (type.equals(Operator.class)) {
            return Operator.DIVIDE;
        } else if (type.equals(Identifier.class)) {
            return TestIds.id((String)generateFirstInstance(String.class));
        } else if (type.equals(PrimitiveValue.class)) {
            return generateFirstInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateFirstInstance(LiteralNode.class);
        } else if (type.equals(Receiver.class)) {
            return generateFirstInstance(InstanceReceiver.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateFirstInstance(VariableReferenceNode.class);
        } else if (type.equals(Type.class)) {
            return generateFirstInstance(ScalarType.class);
        } else {
            return generateValue(type, ValueObjectTests::generateFirstInstance).instance;         
        }
    }
    
    private static Object generateSecondInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string 2]";
        } else if (type.equals(boolean.class)) {
            return false;
        } else if (type.equals(Optional.class)) {
            return Optional.of("[optional 2]");
        } else if (type.equals(List.class)) {
            return list("[list 2]");
        } else if (type.equals(Set.class)) {
            return Collections.singleton("[set 2]");
        } else if (type.equals(Operator.class)) {
            return Operator.MULTIPLY;
        } else if (type.equals(Identifier.class)) {
            return TestIds.id((String)generateSecondInstance(String.class));
        } else if (type.equals(PrimitiveValue.class)) {
            return generateSecondInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateSecondInstance(LiteralNode.class);
        } else if (type.equals(Receiver.class)) {
            return generateSecondInstance(InstanceReceiver.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateSecondInstance(VariableReferenceNode.class);
        } else if (type.equals(Type.class)) {
            return generateSecondInstance(ScalarType.class);
        } else {
            return generateValue(type, ValueObjectTests::generateSecondInstance).instance;           
        }
    }
    
    private static Object generateInstance(Class<?> type) {
        if (type.equals(String.class)) {
            return "[string]";
        } else if (type.equals(boolean.class)) {
            return true;
        } else if (type.equals(Optional.class)) {
            return Optional.of("[optional]");
        } else if (type.equals(List.class)) {
            return list("[list]");
        } else if (type.equals(Set.class)) {
            return Collections.singleton("[set]");
        } else if (type.equals(Operator.class)) {
            return Operator.ADD;
        } else if (type.equals(Identifier.class)) {
            return TestIds.id((String)generateInstance(String.class));
        } else if (type.equals(PrimitiveValue.class)) {
            return generateInstance(StringValue.class);
        } else if (type.equals(ExpressionNode.class)) {
            return generateInstance(LiteralNode.class);
        } else if (type.equals(Receiver.class)) {
            return generateInstance(InstanceReceiver.class);
        } else if (type.equals(AssignableExpressionNode.class)) {
            return generateInstance(VariableReferenceNode.class);
        } else if (type.equals(Type.class)) {
            return generateInstance(ScalarType.class);
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
        return method.getReturnType().isAssignableFrom(type) &&
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
