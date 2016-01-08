package org.zwobble.couscous.backends.python;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.backends.python.ast.PythonBinaryOperation;
import org.zwobble.couscous.backends.python.ast.PythonCallNode;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonNotNode;
import org.zwobble.couscous.values.*;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
import static org.zwobble.couscous.backends.python.ast.PythonBinaryOperation.pythonIs;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonGetSliceNode.pythonGetSlice;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.util.ExtraLists.list;

public class PrimitiveMethods {
    private static final Map<String, PrimitiveMethodGenerator> STRING_METHODS =
        ImmutableMap.<String, PrimitiveMethodGenerator>builder()
        
            .put("length", (receiver, arguments) ->
                pythonCall(
                    pythonVariableReference("len"),
                    list(receiver)))
            
            .put("substring", (receiver, arguments) ->
                pythonGetSlice(receiver, arguments))

            .put("add", (receiver, arguments) ->
                PythonBinaryOperation.pythonAdd(receiver, arguments.get(0)))

            .put("toLowerCase", (receiver, arguments) ->
                pythonCall(pythonAttributeAccess(receiver, "lower"), list()))
            
            .build();
    
    private static final Map<String, PrimitiveMethodGenerator> BOOLEAN_METHODS =
        ImmutableMap.<String, PrimitiveMethodGenerator>builder()
        
            .put("negate", (receiver, arguments) ->
                PythonNotNode.pythonNot(receiver))
            
            .build();
    
    private static final Map<String, PrimitiveMethodGenerator> INT_METHODS;
        
    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();

        methods.put("toString", (receiver, arguments) ->
            pythonCall(pythonVariableReference("str"), list(receiver)));
        
        addOperation(methods, Operator.ADD.getMethodName(), "+");
        addOperation(methods, Operator.SUBTRACT.getMethodName(), "-");
        addOperation(methods, Operator.MULTIPLY.getMethodName(), "*");

        methods.put(Operator.DIVIDE.getMethodName(), (receiver, arguments) ->
            pythonCall(
                internalReference("_div_round_to_zero"),
                list(receiver, arguments.get(0))));
        methods.put(Operator.MOD.getMethodName(), (receiver, arguments) ->
            pythonCall(
                internalReference("_mod_round_to_zero"),
                list(receiver, arguments.get(0))));

        addOperation(methods, Operator.EQUALS.getMethodName(), "==");
        addOperation(methods, Operator.NOT_EQUALS.getMethodName(), "!=");
        addOperation(methods, Operator.GREATER_THAN.getMethodName(), ">");
        addOperation(methods, Operator.GREATER_THAN_OR_EQUAL.getMethodName(), ">=");
        addOperation(methods, Operator.LESS_THAN.getMethodName(), "<");
        addOperation(methods, Operator.LESS_THAN_OR_EQUAL.getMethodName(), "<=");
        
        INT_METHODS = methods.build();
    }
    
    private static PythonExpressionNode internalReference(String name) {
        return pythonAttributeAccess(pythonVariableReference("_couscous"), name);
    }
    
    private static void addOperation(
            Builder<String, PrimitiveMethodGenerator> methods,
            String methodName,
            String operatorSymbol) {
        methods.put(methodName, (receiver, arguments) -> 
            new PythonBinaryOperation(
                operatorSymbol,
                receiver,
                arguments.get(0)));
    }
    
    private static final Map<TypeName, Map<String, PrimitiveMethodGenerator>> METHODS = 
        ImmutableMap.<TypeName, Map<String, PrimitiveMethodGenerator>>builder()
            .put(StringValue.REF, STRING_METHODS)
            .put(BooleanValue.REF, BOOLEAN_METHODS)
            .put(IntegerValue.REF, INT_METHODS)
            .put(ObjectValues.BOXED_INT, INT_METHODS)
            .build();
    
    private static final Map<String, PrimitiveStaticMethodGenerator> INTERNAL_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()
        
            .put("same", arguments -> pythonIs(arguments.get(0), arguments.get(1)))
            
            .build();
    
    private static final Map<TypeName, Map<String, PrimitiveStaticMethodGenerator>> STATIC_METHODS = 
        ImmutableMap.<TypeName, Map<String, PrimitiveStaticMethodGenerator>>builder()
            .put(InternalCouscousValue.REF, INTERNAL_METHODS)
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

    public static Optional<PrimitiveStaticMethodGenerator> getPrimitiveStaticMethod(TypeName type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }
    
    @FunctionalInterface
    public interface PrimitiveStaticMethodGenerator {
        PythonExpressionNode generate(List<PythonExpressionNode> arguments);
    }
}
