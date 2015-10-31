package org.zwobble.couscous.backends.python;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.StringValue;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
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
    
    private static final Map<String, PrimitiveMethodGenerator> INT_METHODS;
        
    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();
        
        addMethod(methods, "add", "__add__");
        addMethod(methods, "subtract", "__sub__");
        addMethod(methods, "multiply", "__mul__");

        methods.put("divide", (receiver, arguments) ->
            pythonCall(
                internalReference("_div_round_to_zero"),
                asList(receiver, arguments.get(0))));
        methods.put("mod", (receiver, arguments) ->
            pythonCall(
                internalReference("_mod_round_to_zero"),
                asList(receiver, arguments.get(0))));

        addMethod(methods, "equals", "__eq__");
        addMethod(methods, "greaterThan", "__gt__");
        addMethod(methods, "greaterThanOrEqual", "__ge__");
        addMethod(methods, "lessThan", "__lt__");
        addMethod(methods, "lessThanOrEqual", "__le__");
        
        INT_METHODS = methods.build();
    }
    
    private static PythonExpressionNode internalReference(String name) {
        return pythonAttributeAccess(pythonVariableReference("_couscous"), name);
    }
    
    private static void addMethod(
            Builder<String, PrimitiveMethodGenerator> methods,
            String methodName,
            String pythonMethodName) {
        methods.put(methodName, (receiver, arguments) -> 
            pythonCall(
                pythonAttributeAccess(receiver, pythonMethodName),
                arguments));
    }
    
    private static final Map<TypeName, Map<String, PrimitiveMethodGenerator>> METHODS = 
        ImmutableMap.<TypeName, Map<String, PrimitiveMethodGenerator>>builder()
            .put(StringValue.REF, STRING_METHODS)
            .put(IntegerValue.REF, INT_METHODS)
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
