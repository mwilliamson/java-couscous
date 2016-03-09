package org.zwobble.couscous.tests.interpreter;

import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.JavaProject;
import org.zwobble.couscous.interpreter.Project;
import org.zwobble.couscous.interpreter.errors.NoSuchMethod;
import org.zwobble.couscous.interpreter.errors.UnboundVariable;
import org.zwobble.couscous.interpreter.errors.UnexpectedValueType;
import org.zwobble.couscous.interpreter.errors.VariableNotInScope;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValues;
import org.zwobble.couscous.tests.BackendMethodTests;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;
import org.zwobble.couscous.values.UnitValue;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodNode.staticMethod;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;
import static org.zwobble.couscous.util.ExtraLists.list;
import static org.zwobble.couscous.values.PrimitiveValues.value;

public class InterpreterMethodTests extends BackendMethodTests {
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        MethodNode.Builder method = staticMethod("hello");
        
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> runMethod(method, value("hello, world!")));
        
        assertEquals(new MethodSignature("hello", list(StringValue.REF), UnitValue.REF), exception.getSignature());
    }
    
    @Test
    public void errorIfTryingToAssignValueOfWrongTypeToVariable() {
        FormalArgumentNode arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        MethodNode.Builder method = staticMethod("hello")
            .argument(arg)
            .statement(expressionStatement(assign(reference(arg), literal(0))));

        UnexpectedValueType exception = assertThrows(UnexpectedValueType.class,
            () -> runMethod(method, value("")));
        
        assertEquals(new UnexpectedValueType(StringValue.REF, IntegerValue.REF), exception);
    }
    
    @Test
    public void errorIfTryingToAssignToVariableNotInScope() {
        LocalVariableDeclarationNode localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        MethodNode.Builder method = staticMethod("hello")
            .statement(expressionStatement(assign(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))));

        VariableNotInScope exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(ANY_ID), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfVariableNotInScope() {
        LocalVariableDeclarationNode localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        MethodNode.Builder method = staticMethod("hello")
            .statement(returns(reference(localVariableDeclaration)));

        VariableNotInScope exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(ANY_ID), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfUnboundVariable() {
        LocalVariableDeclarationNode localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        MethodNode.Builder method = staticMethod("hello")
            .statement(returns(reference(localVariableDeclaration)))
            .statement(localVariableDeclaration);

        UnboundVariable exception = assertThrows(UnboundVariable.class,
            () -> runMethod(method));
        
        assertEquals(new UnboundVariable(ANY_ID), exception);
    }

    @Override
    protected MethodRunner buildMethodRunner() {
        return new MethodRunner() {
            @Override
            public PrimitiveValue runMethod(
                List<TypeNode> classNodes,
                ScalarType className,
                String methodName,
                List<PrimitiveValue> arguments,
                Type returnType) {
                
                Project project = JavaProject.of(classNodes);
                Interpreter interpreter = new Interpreter(project);
                List<InterpreterValue> argumentValues = arguments.stream()
                    .map(InterpreterValues::value)
                    .collect(Collectors.toList());
                
                return interpreter.run(className, methodName, argumentValues, returnType)
                    .toPrimitiveValue().get();
            }
        };
    }
}
