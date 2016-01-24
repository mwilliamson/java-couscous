package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.values.*;

import java.util.Map;
import java.util.Optional;

import static org.zwobble.couscous.util.Casts.tryCast;

public class CsharpCodeGenerator {
    private final static Map<TypeName, TypeName> PRIMITIVES = ImmutableMap.<TypeName, TypeName>builder()
        .put(IntegerValue.REF, TypeName.of("int"))
        .put(StringValue.REF, TypeName.of("string"))
        .put(BooleanValue.REF, TypeName.of("bool"))
        .put(ObjectValues.OBJECT, TypeName.of("object"))
        .put(ObjectValues.CLASS, TypeName.of("System.Type"))
        .put(UnitValue.REF, TypeName.of("void"))
        .build();

    private final String namespace;

    public static Node generateCode(Node node, String namespace) {
        return new CsharpCodeGenerator(namespace).generateCode(node);
    }

    private CsharpCodeGenerator(String namespace) {
        this.namespace = namespace;
    }

    private Node generateCode(Node node) {
        return NodeTransformer.builder()
            .transformType(this::transformType)
            .transformExpression(this::transformExpression)
            .build()
            .transform(node);
    }

    private TypeName transformType(TypeName type) {
        if (PRIMITIVES.containsKey(type)) {
            return PRIMITIVES.get(type);
        } else {
            return addPrefix(type, namespace);
        }
    }

    private Optional<ExpressionNode> transformExpression(ExpressionNode expression) {
        return tryCast(MethodCallNode.class, expression)
            .flatMap(this::transformMethodCall);
    }

    private Optional<ExpressionNode> transformMethodCall(MethodCallNode call) {
        return call.getReceiver().accept(new Receiver.Mapper<Optional<ExpressionNode>>() {
            @Override
            public Optional<ExpressionNode> visit(ExpressionNode receiver) {
                return CsharpPrimitiveMethods.getPrimitiveMethod(receiver.getType(), call.getMethodName())
                    .map(generator -> generator.generate(receiver, call.getArguments()));
            }

            @Override
            public Optional<ExpressionNode> visit(TypeName receiver) {
                return CsharpPrimitiveMethods.getPrimitiveStaticMethod(receiver, call.getMethodName())
                    .map(generator -> generator.generate(call.getArguments()));
            }
        });
    }

    private TypeName addPrefix(TypeName name, String namespace) {
        return TypeName.of(namespace + "." + name.getQualifiedName());
    }
}
