package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.InternalCouscousValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.OperationNode.operation;
import static org.zwobble.couscous.ast.Operations.integerSubtract;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpPrimitiveMethods {
    private static final Map<String, PrimitiveMethodGenerator> BOOLEAN_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();
        BOOLEAN_METHODS = methods.build();
    }

    private static final Map<String, PrimitiveMethodGenerator> BOXED_INT_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();
        methods.put("toString", ((receiver, arguments) -> methodCall(
            receiver,
            "ToString",
            list(),
            Types.STRING)));
        BOXED_INT_METHODS = methods.build();
    }

    private static final Map<String, PrimitiveMethodGenerator> STRING_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();
        methods.put("length", (receiver, arguments) -> fieldAccess(receiver, "Length", Types.INT));
        methods.put("substring", (receiver, arguments) -> {
            ExpressionNode startIndex = arguments.get(0);
            ExpressionNode endIndex = arguments.get(1);
            ExpressionNode length = integerSubtract(endIndex, startIndex);
            // TODO: handle length going beyond end-of-string
            return methodCall(
                receiver,
                "Substring",
                list(startIndex, length),
                Types.STRING);
        });
        methods.put("toLowerCase", (receiver, arguments) -> methodCall(
            receiver,
            "ToLower",
            list(),
            Types.STRING));
        methods.put("equals", (receiver, arguments) -> methodCall(
            receiver,
            "Equals",
            arguments,
            Types.STRING));
        STRING_METHODS = methods.build();
    }

    private static final Map<ScalarType, Map<String, PrimitiveMethodGenerator>> METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveMethodGenerator>>builder()
            .put(Types.BOOLEAN, BOOLEAN_METHODS)
            .put(Types.STRING, STRING_METHODS)
            .put(Types.BOXED_INT, BOXED_INT_METHODS)
            .build();

    private static final Map<String, PrimitiveStaticMethodGenerator> STATIC_INT_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("parseInt", arguments -> staticMethodCall(
                ScalarType.of("int"),
                "Parse",
                list(arguments.get(0)),
                ScalarType.of("int")))

            .put("valueOf", arguments -> arguments.get(0))

            .build();

    private static final Map<String, PrimitiveStaticMethodGenerator> STATIC_STRING_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("join", arguments -> staticMethodCall(
                ScalarType.of("java.lang.String"),
                "join",
                arguments,
                Types.STRING))

            .build();

    private static final Map<String, PrimitiveStaticMethodGenerator> STATIC_INTERNAL_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("same", arguments -> operation(Operator.EQUALS, arguments, Types.BOOLEAN))

            .build();

    private static final Map<ScalarType, Map<String, PrimitiveStaticMethodGenerator>> STATIC_METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveStaticMethodGenerator>>builder()
            .put(Types.BOXED_INT, STATIC_INT_METHODS)
            .put(Types.STRING, STATIC_STRING_METHODS)
            .put(InternalCouscousValue.REF, STATIC_INTERNAL_METHODS)
            .build();

    public static Optional<PrimitiveMethodGenerator> getPrimitiveMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    @FunctionalInterface
    public interface PrimitiveMethodGenerator {
        ExpressionNode generate(ExpressionNode receiver, List<ExpressionNode> arguments);
    }

    public static Optional<PrimitiveStaticMethodGenerator> getPrimitiveStaticMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    @FunctionalInterface
    public interface PrimitiveStaticMethodGenerator {
        ExpressionNode generate(List<ExpressionNode> arguments);
    }
}
