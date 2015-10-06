package org.zwobble.couscous.backends.python;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonGetSliceNode.pythonGetSlice;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;

public class PrimitiveMethods {
    private static final Map<String, PrimitiveMethodGenerator> STRING_METHODS =
        ImmutableMap.<String, PrimitiveMethodGenerator>builder()
        
            .put("length", (receiver, arguments) ->
                pythonCall(
                    pythonVariableReference("len"),
                    asList(receiver)))
            
            .put("substring", (receiver, arguments) ->
                pythonGetSlice(receiver, arguments))
            
            .build();
    
    private static final Map<TypeName, Map<String, PrimitiveMethodGenerator>> METHODS = 
        ImmutableMap.<TypeName, Map<String, PrimitiveMethodGenerator>>builder()
            .put(StringValue.REF, STRING_METHODS)
            .build();
    
    public static boolean isPrimitive(TypeName type) {
        return METHODS.containsKey(type);
    }
    
    public static Optional<PrimitiveMethodGenerator> getPrimitiveMethod(TypeName type, String methodName) {
        return Optional.ofNullable(METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }
    
    @FunctionalInterface
    public interface PrimitiveMethodGenerator {
        PythonExpressionNode generate(PythonExpressionNode receiver, List<PythonExpressionNode> arguments);
    }
}
