package org.zwobble.couscous.backends.python;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.zwobble.couscous.ast.Operator;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.backends.python.ast.PythonBinaryOperation;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonNotNode;
import org.zwobble.couscous.values.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
import static org.zwobble.couscous.backends.python.ast.PythonBinaryOperation.pythonIs;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonGetSliceNode.pythonGetSlice;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.util.ExtraLists.list;

public class PythonPrimitiveMethods {
    private static final Map<String, PrimitiveMethodGenerator> STRING_METHODS =
        ImmutableMap.<String, PrimitiveMethodGenerator>builder()
        
            .put("length", (receiver, arguments) ->
                pythonCall(
                    pythonVariableReference("len"),
                    list(receiver)))
            
            .put("substring", (receiver, arguments) ->
                pythonGetSlice(receiver, arguments))

            .put(Operator.ADD.getSymbol(), (receiver, arguments) ->
                PythonBinaryOperation.pythonAdd(receiver, arguments.get(0)))

            .put("toLowerCase", (receiver, arguments) ->
                pythonCall(pythonAttributeAccess(receiver, "lower"), list()))

            .put("equals", (receiver, arguments) ->
                PythonBinaryOperation.pythonEqual(receiver, arguments.get(0)))

            .build();
    
    private static final Map<String, PrimitiveMethodGenerator> BOOLEAN_METHODS;

    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();

        methods.put(Operator.BOOLEAN_NOT.getSymbol(), (receiver, arguments) ->
            PythonNotNode.pythonNot(receiver));

        addOperation(methods, Operator.BOOLEAN_AND, PythonBinaryOperation::pythonAnd);
        addOperation(methods, Operator.BOOLEAN_OR, PythonBinaryOperation::pythonOr);

        BOOLEAN_METHODS = methods.build();
    }
    
    private static final Map<String, PrimitiveMethodGenerator> INT_METHODS;
        
    static {
        ImmutableMap.Builder<String, PrimitiveMethodGenerator> methods = ImmutableMap.builder();

        methods.put("toString", (receiver, arguments) ->
            pythonCall(pythonVariableReference("str"), list(receiver)));
        
        addOperation(methods, Operator.ADD, PythonBinaryOperation::pythonAdd);
        addOperation(methods, Operator.SUBTRACT, PythonBinaryOperation::pythonSubtract);
        addOperation(methods, Operator.MULTIPLY, PythonBinaryOperation::pythonMultiply);

        methods.put(Operator.DIVIDE.getSymbol(), (receiver, arguments) ->
            pythonCall(
                internalReference("_div_round_to_zero"),
                list(receiver, arguments.get(0))));
        methods.put(Operator.MOD.getSymbol(), (receiver, arguments) ->
            pythonCall(
                internalReference("_mod_round_to_zero"),
                list(receiver, arguments.get(0))));

        addOperation(methods, Operator.EQUALS, PythonBinaryOperation::pythonEqual);
        addOperation(methods, Operator.NOT_EQUALS, PythonBinaryOperation::pythonNotEqual);
        addOperation(methods, Operator.GREATER_THAN, PythonBinaryOperation::pythonGreaterThan);
        addOperation(methods, Operator.GREATER_THAN_OR_EQUAL, PythonBinaryOperation::pythonGreatThanOrEqual);
        addOperation(methods, Operator.LESS_THAN, PythonBinaryOperation::pythonLessThan);
        addOperation(methods, Operator.LESS_THAN_OR_EQUAL, PythonBinaryOperation::pythonLessThanOrEqual);
        
        INT_METHODS = methods.build();
    }
    
    private static PythonExpressionNode internalReference(String name) {
        return pythonAttributeAccess(pythonVariableReference("_couscous"), name);
    }

    private static void addOperation(
        Builder<String, PrimitiveMethodGenerator> methods,
        Operator operator,
        BiFunction<PythonExpressionNode, PythonExpressionNode, PythonExpressionNode> build)
    {
        addOperation(methods, operator.getSymbol(), build);
    }
    
    private static void addOperation(
        Builder<String, PrimitiveMethodGenerator> methods,
        String methodName,
        BiFunction<PythonExpressionNode, PythonExpressionNode, PythonExpressionNode> build)
    {
        methods.put(methodName, (receiver, arguments) -> 
            build.apply(receiver, arguments.get(0)));
    }
    
    private static final Map<ScalarType, Map<String, PrimitiveMethodGenerator>> METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveMethodGenerator>>builder()
            .put(StringValue.REF, STRING_METHODS)
            .put(BooleanValue.REF, BOOLEAN_METHODS)
            .put(IntegerValue.REF, INT_METHODS)
            .put(ObjectValues.BOXED_INT, INT_METHODS)
            .build();
    
    private static final Map<String, PrimitiveStaticMethodGenerator> INTERNAL_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()
        
            .put("same", arguments -> pythonIs(arguments.get(0), arguments.get(1)))
            
            .build();

    private static final Map<String, PrimitiveStaticMethodGenerator> BOXED_INT_STATIC_METHODS =
        ImmutableMap.<String, PrimitiveStaticMethodGenerator>builder()

            .put("parseInt", arguments -> pythonCall(pythonVariableReference("int"), arguments))

            .build();
    
    private static final Map<ScalarType, Map<String, PrimitiveStaticMethodGenerator>> STATIC_METHODS =
        ImmutableMap.<ScalarType, Map<String, PrimitiveStaticMethodGenerator>>builder()
            .put(InternalCouscousValue.REF, INTERNAL_METHODS)
            .put(ObjectValues.BOXED_INT, BOXED_INT_STATIC_METHODS)
            .build();

    public static Optional<PrimitiveMethodGenerator> getPrimitiveMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }
    
    @FunctionalInterface
    public interface PrimitiveMethodGenerator {
        PythonExpressionNode generate(PythonExpressionNode receiver, List<PythonExpressionNode> arguments);
    }

    public static Optional<PrimitiveStaticMethodGenerator> getPrimitiveStaticMethod(ScalarType type, String methodName) {
        return Optional.ofNullable(STATIC_METHODS.get(type))
            .flatMap(methodsForType -> Optional.ofNullable(methodsForType.get(methodName)));
    }
    
    @FunctionalInterface
    public interface PrimitiveStaticMethodGenerator {
        PythonExpressionNode generate(List<PythonExpressionNode> arguments);
    }
}
