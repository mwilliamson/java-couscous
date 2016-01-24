package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpPrimitiveMethods {
    private static final Map<String, PrimitiveMethodGenerator> BOOLEAN_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();
        BOOLEAN_METHODS = methods.build();
    }

    private static final Map<TypeName, Map<String, PrimitiveMethodGenerator>> METHODS =
        ImmutableMap.<TypeName, Map<String, PrimitiveMethodGenerator>>builder()
            .put(BooleanValue.REF, BOOLEAN_METHODS)
            .build();

    private static final Map<String, PrimitiveStaticMethodGenerator> STATIC_INT_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("parseInt", arguments -> staticMethodCall(
                TypeName.of("int"),
                "Parse",
                list(arguments.get(0)),
                TypeName.of("int")))

            .build();

    private static final Map<TypeName, Map<String, PrimitiveStaticMethodGenerator>> STATIC_METHODS =
        ImmutableMap.<TypeName, Map<String, PrimitiveStaticMethodGenerator>>builder()
            .put(ObjectValues.BOXED_INT, STATIC_INT_METHODS)
            .build();

    public static Optional<PrimitiveMethodGenerator> getPrimitiveMethod(TypeName type, String methodName) {
        return Optional.ofNullable(METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    @FunctionalInterface
    public interface PrimitiveMethodGenerator {
        ExpressionNode generate(ExpressionNode receiver, List<ExpressionNode> arguments);
    }

    public static Optional<PrimitiveStaticMethodGenerator> getPrimitiveStaticMethod(TypeName type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    @FunctionalInterface
    public interface PrimitiveStaticMethodGenerator {
        ExpressionNode generate(List<ExpressionNode> arguments);
    }
}
