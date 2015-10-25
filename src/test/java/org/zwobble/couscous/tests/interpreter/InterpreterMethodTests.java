package org.zwobble.couscous.tests.interpreter;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.Interpreter;
import org.zwobble.couscous.interpreter.JavaProject;
import org.zwobble.couscous.interpreter.UnboundVariable;
import org.zwobble.couscous.interpreter.UnexpectedValueType;
import org.zwobble.couscous.interpreter.VariableNotInScope;
import org.zwobble.couscous.interpreter.WrongNumberOfArguments;
import org.zwobble.couscous.interpreter.values.InterpreterValues;
import org.zwobble.couscous.tests.BackendMethodTests;
import org.zwobble.couscous.tests.MethodRunner;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.StringValue;

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
import static org.zwobble.couscous.values.PrimitiveValues.value;

import lombok.val;

public class InterpreterMethodTests extends BackendMethodTests {
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        val method = staticMethod("hello");
        
        val exception = assertThrows(WrongNumberOfArguments.class,
            () -> runMethod(method, value("hello, world!")));
        
        assertEquals(new WrongNumberOfArguments(0, 1), exception);
    }
    
    @Test
    public void errorIfTryingToAssignValueOfWrongTypeToVariable() {
        val arg = formalArg(var(ANY_ID, "x", StringValue.REF));
        val method = staticMethod("hello")
            .argument(arg)
            .statement(expressionStatement(assign(reference(arg), literal(0))));

        val exception = assertThrows(UnexpectedValueType.class,
            () -> runMethod(method, value("")));
        
        assertEquals(new UnexpectedValueType(StringValue.REF, IntegerValue.REF), exception);
    }
    
    @Test
    public void errorIfTryingToAssignToVariableNotInScope() {
        val localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(expressionStatement(assign(reference(localVariableDeclaration), LiteralNode.literal("[updated value]"))));

        val exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(ANY_ID), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfVariableNotInScope() {
        val localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(returns(reference(localVariableDeclaration)));

        val exception = assertThrows(VariableNotInScope.class,
            () -> runMethod(method));
        
        assertEquals(new VariableNotInScope(ANY_ID), exception);
    }
    
    @Test
    public void errorIfTryingToGetValueOfUnboundVariable() {
        val localVariableDeclaration = localVariableDeclaration(
            ANY_ID, "x", StringValue.REF, literal(""));
        val method = staticMethod("hello")
            .statement(returns(reference(localVariableDeclaration)))
            .statement(localVariableDeclaration);

        val exception = assertThrows(UnboundVariable.class,
            () -> runMethod(method));
        
        assertEquals(new UnboundVariable(ANY_ID), exception);
    }

    @Override
    protected MethodRunner buildMethodRunner() {
        return new MethodRunner() {
            @Override
            public PrimitiveValue runMethod(
                    List<ClassNode> classNodes,
                    TypeName className,
                    String methodName,
                    List<PrimitiveValue> arguments) {
                
                val project = JavaProject.of(classNodes);
                val interpreter = new Interpreter(project);
                val argumentValues = arguments.stream()
                    .map(InterpreterValues::value)
                    .collect(Collectors.toList());
                
                return interpreter.run(className, methodName, argumentValues)
                    .toPrimitiveValue().get();
            }
        };
    }
}
