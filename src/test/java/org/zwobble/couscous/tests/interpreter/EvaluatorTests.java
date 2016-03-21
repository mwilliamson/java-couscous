package org.zwobble.couscous.tests.interpreter;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.interpreter.Environment;
import org.zwobble.couscous.interpreter.JavaProject;
import org.zwobble.couscous.interpreter.MapBackedProject;
import org.zwobble.couscous.interpreter.StackFrameBuilder;
import org.zwobble.couscous.interpreter.errors.*;
import org.zwobble.couscous.interpreter.values.IntegerInterpreterValue;
import org.zwobble.couscous.interpreter.values.InterpreterValue;
import org.zwobble.couscous.interpreter.values.StringInterpreterValue;
import org.zwobble.couscous.tests.BackendEvalTests;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.PrimitiveValue;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.zwobble.couscous.ast.AssignmentNode.assign;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.CastNode.cast;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.LiteralNode.literal;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.TernaryConditionalNode.ternaryConditional;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.interpreter.Evaluator.eval;
import static org.zwobble.couscous.interpreter.values.InterpreterValues.value;
import static org.zwobble.couscous.tests.TestIds.ANY_ID;
import static org.zwobble.couscous.tests.util.ExtraAsserts.assertThrows;
import static org.zwobble.couscous.util.ExtraLists.list;

public class EvaluatorTests extends BackendEvalTests {
    @Test
    public void valueOfAssignmentExpressionIsNewValue() {
        FormalArgumentNode arg = formalArg(var(ANY_ID, "x", Types.STRING));
        Environment environment = new Environment(
            new MapBackedProject(ImmutableMap.of()),
            Optional.empty(),
            new StackFrameBuilder().declare(arg, value("[initial value]")).build());
        InterpreterValue result = eval(environment, assign(arg, literal("[updated value]")));
        assertEquals(StringInterpreterValue.of("[updated value]"), result);
    }
    
    @Test
    public void errorIfConditionIsNotBoolean() {
        ConditionMustBeBoolean exception = assertThrows(ConditionMustBeBoolean.class,
            () -> eval(emptyEnvironment(),
                ternaryConditional(literal(1), literal("T"), literal("F"))));
        assertEquals(new ConditionMustBeBoolean(new IntegerInterpreterValue(1)), exception);
    }
    
    @Test
    public void errorIfMethodDoesNotExist() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "size",
                    list(),
                    Types.INT)));
        assertEquals(new MethodSignature("size", list(), Types.INT), exception.getSignature());
    }
    
    @Test
    public void errorIfWrongNumberOfArgumentsArePassed() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    list(literal(1)),
                    Types.STRING)));
        assertEquals(new MethodSignature("substring", list(Types.INT), Types.STRING), exception.getSignature());
    }
    
    @Test
    public void errorIfArgumentIsWrongType() {
        NoSuchMethod exception = assertThrows(NoSuchMethod.class,
            () -> eval(emptyEnvironment(),
                methodCall(
                    literal("hello"),
                    "substring",
                    list(literal(0), literal("")),
                    Types.STRING)));
        assertEquals(
            new MethodSignature("substring", list(Types.INT, Types.STRING), Types.STRING),
            exception.getSignature());
    }

    @Test
    public void errorIfConstructorArgumentIsWrongType() {
        FormalArgumentNode argument = formalArg(var(ANY_ID, "x", Types.INT));
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .constructor(constructor -> constructor
                .argument(argument))
            .build();

        UnexpectedValueType exception = assertThrows(UnexpectedValueType.class,
            () -> evalExpression(
                list(classNode),
                constructorCall(
                    classNode.getName(),
                    list(literal("")))));

        assertEquals(new UnexpectedValueType(Types.INT, Types.STRING), exception);
    }

    @Test
    public void cannotGetValueOfUndeclaredField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .build();
        
        NoSuchField exception = assertThrows(NoSuchField.class,
            () -> evalExpression(
                list(classNode),
                fieldAccess(
                    constructorCall(
                        classNode.getName(),
                        list()),
                    "value",
                    Types.INT)));
        
        assertEquals(new NoSuchField("value"), exception);
    }
    
    @Test
    public void cannotGetValueOfUnboundField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", Types.INT)
            .build();
        
        UnboundField exception = assertThrows(UnboundField.class,
            () -> evalExpression(
                list(classNode),
                fieldAccess(
                    constructorCall(
                        classNode.getName(),
                        list()),
                    "value",
                    Types.INT)));
        
        assertEquals(new UnboundField("value"), exception);
    }
    
    @Test
    public void cannotSetValueOfUndeclaredField() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .constructor(constructor -> constructor
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", Types.INT),
                    literal(42))))
            .build();
        
        NoSuchField exception = assertThrows(NoSuchField.class,
            () -> evalExpression(
                list(classNode),
                constructorCall(
                    classNode.getName(),
                    list())));
        
        assertEquals(new NoSuchField("value"), exception);
    }

    @Test
    public void cannotSetValueOfInstanceFieldAsThoughItIsAStaticField() {
        ScalarType type = ScalarType.of("com.example.Example");
        ClassNode classNode = ClassNode.builder(type)
            .field("value", Types.INT)
            .constructor(constructor -> constructor
                .statement(assignStatement(
                    fieldAccess(type, "value", Types.INT),
                    literal(42))))
            .build();

        NoSuchField exception = assertThrows(NoSuchField.class,
            () -> evalExpression(
                list(classNode),
                constructorCall(
                    classNode.getName(),
                    list())));

        assertEquals(new NoSuchField("value"), exception);
    }
    
    @Test
    public void cannotSetValueOfFieldWithWrongType() {
        ClassNode classNode = ClassNode.builder("com.example.Example")
            .field("value", Types.INT)
            .constructor(constructor -> constructor
                .statement(assignStatement(
                    fieldAccess(constructor.thisReference(), "value", Types.INT),
                    literal(""))))
            .build();
        
        UnexpectedValueType exception = assertThrows(UnexpectedValueType.class,
            () -> evalExpression(
                list(classNode),
                constructorCall(
                    classNode.getName(),
                    list())));
        
        assertEquals(new UnexpectedValueType(Types.INT, Types.STRING), exception);
    }

    @Test
    public void validCastPassesValueThrough() {
        InterpreterValue value = eval(
            emptyEnvironment(),
            cast(literal("42"), Types.OBJECT));
        assertEquals(value("42"), value);
    }

    @Test
    public void errorIfCastIsNotValid() {
        InvalidCast exception = assertThrows(InvalidCast.class,
            () -> eval(emptyEnvironment(),
                cast(literal(42), Types.STRING)));
        assertEquals(Types.STRING, exception.getExpected());
        assertEquals(Types.INT, exception.getActual());
    }
    
    private static Environment emptyEnvironment() {
        return new Environment(
            JavaProject.builder().build(),
            Optional.empty(),
            ImmutableMap.of());
    }

    @Override
    protected PrimitiveValue evalExpression(
            List<TypeNode> classes,
            ExpressionNode expression) {
        
        Environment environment = new Environment(
            JavaProject.of(classes),
            Optional.empty(),
            ImmutableMap.of());
        return eval(environment, expression).toPrimitiveValue().get();
    }
}
