package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.values.*;

import java.util.Map;
import java.util.Optional;

public class CsharpCodeGenerator extends NodeTransformer {
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
        return node.accept(this);
    }

    @Override
    public TypeName transform(TypeName type) {
        if (PRIMITIVES.containsKey(type)) {
            return PRIMITIVES.get(type);
        } else {
            return addPrefix(type, namespace);
        }
    }

    @Override
    public ExpressionNode visit(MethodCallNode call) {
        return toPrimitiveMethod(call).orElseGet(() -> super.visit(call));
    }

    private Optional<ExpressionNode> toPrimitiveMethod(MethodCallNode call) {
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
