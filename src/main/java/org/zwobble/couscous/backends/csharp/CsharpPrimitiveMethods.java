package org.zwobble.couscous.backends.csharp;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.SourceCodeWriter;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CsharpPrimitiveMethods {
    private static final Map<String, PrimitiveStaticMethodGenerator> STATIC_INT_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("parseInt", (arguments, writer) -> {
                writer.writeIdentifier("int");
                writer.writeSymbol(".");
                writer.writeIdentifier("Parse");
                writer.writeSymbol("(");
                writer.writeIdentifier(CsharpSerializer.serialize(arguments.get(0), ""));
                writer.writeSymbol(")");
            })

            .build();

    private static final Map<TypeName, Map<String, PrimitiveStaticMethodGenerator>> STATIC_METHODS =
        ImmutableMap.<TypeName, Map<String, PrimitiveStaticMethodGenerator>>builder()
            .put(ObjectValues.BOXED_INT, STATIC_INT_METHODS)
            .build();

    public static Optional<PrimitiveStaticMethodGenerator> getPrimitiveStaticMethod(TypeName type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }

    @FunctionalInterface
    public interface PrimitiveStaticMethodGenerator {
        void generate(List<ExpressionNode> arguments, SourceCodeWriter writer);
    }
}
