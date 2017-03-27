package org.zwobble.couscous.backends.csharp.primitives;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.InternalCouscousValue;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.MethodCallNode.staticMethodCall;
import static org.zwobble.couscous.ast.OperationNode.operation;
import static org.zwobble.couscous.ast.Operations.boxInt;
import static org.zwobble.couscous.ast.Operations.integerSubtract;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.list;

public class CsharpPrimitiveMethods {
    public static final NodeTransformer TRANSFORMER = NodeTransformer.builder()
        .transformExpression(CsharpPrimitiveMethods::transformExpression)
        .build();


    private static Optional<ExpressionNode> transformExpression(ExpressionNode expression) {
        return tryCast(MethodCallNode.class, expression)
            .flatMap(CsharpPrimitiveMethods::transformMethodCall);
    }

    private static Optional<ExpressionNode> transformMethodCall(MethodCallNode call) {
        return call.getReceiver().accept(new Receiver.Mapper<Optional<ExpressionNode>>() {
            @Override
            public Optional<ExpressionNode> visit(ExpressionNode receiver) {
                return CsharpPrimitiveMethods.getPrimitiveMethod(erasure(receiver.getType()), call.getMethodName())
                    .map(method -> new PrimitiveInstanceMethodCall(method, receiver, call.getArguments()));
            }

            @Override
            public Optional<ExpressionNode> visit(ScalarType receiver) {
                return CsharpPrimitiveMethods.getPrimitiveStaticMethod(receiver, call.getMethodName())
                    .map(method -> new PrimitiveStaticMethodCall(method, call.getArguments()));
            }
        });
    }

    private static final Map<String, PrimitiveInstanceMethod> BOOLEAN_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveInstanceMethod> methods = ImmutableMap.builder();
        BOOLEAN_METHODS = methods.build();
    }

    private static final Map<String, PrimitiveInstanceMethod> BOXED_INT_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveInstanceMethod> methods = ImmutableMap.builder();
        methods.put("toString", new PrimitiveInstanceMethod(
            (receiver, arguments) -> methodCall(
                receiver,
                "ToString",
                list(),
                Types.STRING
            ),
            Types.STRING
        ));
        BOXED_INT_METHODS = methods.build();
    }

    private static final Map<String, PrimitiveInstanceMethod> STRING_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveInstanceMethod> methods = ImmutableMap.builder();
        methods.put("length", new PrimitiveInstanceMethod(
            (receiver, arguments) -> fieldAccess(receiver, "Length", Types.INT),
            Types.INT
        ));
        methods.put("substring", new PrimitiveInstanceMethod(
            (receiver, arguments) -> {
                ExpressionNode startIndex = arguments.get(0);
                if (arguments.size() == 1) {
                    return methodCall(
                        receiver,
                        "Substring",
                        list(startIndex),
                        Types.STRING
                    );
                } else {
                    ExpressionNode endIndex = arguments.get(1);
                    ExpressionNode length = integerSubtract(endIndex, startIndex);
                    // TODO: handle length going beyond end-of-string
                    return methodCall(
                        receiver,
                        "Substring",
                        list(startIndex, length),
                        Types.STRING
                    );
                }
            },
            Types.STRING
        ));
        methods.put("toLowerCase", new PrimitiveInstanceMethod(
            (receiver, arguments) -> methodCall(
                receiver,
                "ToLower",
                list(),
                Types.STRING
            ),
            Types.STRING
        ));
        methods.put("equals", new PrimitiveInstanceMethod(
            (receiver, arguments) -> methodCall(
                receiver,
                "Equals",
                arguments,
                Types.STRING
            ),
            Types.STRING
        ));
        STRING_METHODS = methods.build();
    }

    private static final Map<ScalarType, Map<String, PrimitiveInstanceMethod>> METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveInstanceMethod>>builder()
            .put(Types.BOOLEAN, BOOLEAN_METHODS)
            .put(Types.STRING, STRING_METHODS)
            .put(Types.BOXED_INT, BOXED_INT_METHODS)
            .build();

    private static final Map<String, PrimitiveStaticMethod> STATIC_INT_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethod>builder()

            .put("toString", new PrimitiveStaticMethod(
                arguments -> methodCall(
                    arguments.get(0),
                    "ToString",
                    list(),
                    Types.STRING
                ),
                Types.STRING
            ))

            .put("parseInt", new PrimitiveStaticMethod(
                arguments -> staticMethodCall(
                    ScalarType.topLevel("int"),
                    "Parse",
                    list(arguments.get(0)),
                    ScalarType.topLevel("int")
                ),
                Types.INT
            ))

            .put("valueOf", new PrimitiveStaticMethod(
                arguments -> boxInt(arguments.get(0)),
                Types.BOXED_INT
            ))

            .build();

    private static final Map<String, PrimitiveStaticMethod> STATIC_STRING_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethod>builder()

            .put("join", new PrimitiveStaticMethod(
                arguments -> staticMethodCall(
                    ScalarType.topLevel("java.lang.String"),
                    "join",
                    arguments,
                    Types.STRING
                ),
                Types.STRING
            ))

            .build();

    private static final Map<String, PrimitiveStaticMethod> STATIC_INTERNAL_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethod>builder()

            .put("same", new PrimitiveStaticMethod(
                arguments -> operation(Operator.EQUALS, arguments, Types.BOOLEAN),
                Types.BOOLEAN
            ))

            .build();

    private static final Map<ScalarType, Map<String, PrimitiveStaticMethod>> STATIC_METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveStaticMethod>>builder()
            .put(Types.BOXED_INT, STATIC_INT_METHODS)
            .put(Types.STRING, STATIC_STRING_METHODS)
            .put(InternalCouscousValue.REF, STATIC_INTERNAL_METHODS)
            .build();

    private static Optional<PrimitiveInstanceMethod> getPrimitiveMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    private static Optional<PrimitiveStaticMethod> getPrimitiveStaticMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

}
