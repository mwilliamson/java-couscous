package org.zwobble.couscous.interpreter;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import com.google.common.collect.ImmutableMap;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.StaticMethodCallNode.staticMethodCall;
import static org.zwobble.couscous.interpreter.Evaluator.eval;

public class Interpreter {
    private Project project;
    
    public Interpreter(Project project) {
        this.project = project;
    }
    
    public InterpreterValue run(TypeName className, String methodName, List<InterpreterValue> arguments) {
        List<ExpressionNode> argumentExpressions = arguments.stream()
            .map(argument -> literal(argument.toPrimitiveValue().get()))
            .collect(Collectors.toList());
        return eval(
            new Environment(
                project,
                Optional.empty(),
                ImmutableMap.of()),
            staticMethodCall(className, methodName, argumentExpressions));
    }
}