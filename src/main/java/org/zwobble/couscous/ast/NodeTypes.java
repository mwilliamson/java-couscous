package org.zwobble.couscous.ast;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.backends.csharp.CsharpNodeTypes;
import org.zwobble.couscous.backends.csharp.primitives.PrimitiveInstanceMethodCall;
import org.zwobble.couscous.backends.csharp.primitives.PrimitiveStaticMethodCall;

import java.util.Map;

public class NodeTypes {
    public static final int ANNOTATION = 10000;
    public static final int ARRAY = 10001;
    public static final int ASSIGNMENT = 10002;
    public static final int CAST = 10003;
    public static final int CLASS = 10004;
    public static final int CONSTRUCTOR_CALL = 10005;
    public static final int CONSTRUCTOR = 10006;
    public static final int ENUM = 10007;
    public static final int EXCEPTION_HANDLER = 10008;
    public static final int EXPRESSION_STATEMENT = 10009;
    public static final int FIELD_ACCESS = 10010;
    public static final int FIELD_DECLARATION = 10011;
    public static final int FORMAL_ARGUMENT = 10012;
    public static final int FORMAL_TYPE_PARAMETER = 10013;
    public static final int IF_STATEMENT = 10014;
    public static final int INSTANCE_OF = 10015;
    public static final int INSTANCE_RECEIVER = 10016;
    public static final int INTERFACE = 10017;
    public static final int LITERAL = 10018;
    public static final int LOCAL_VARIABLE_DECLARATION = 10019;
    public static final int METHOD_CALL = 10020;
    public static final int METHOD = 10031;
    public static final int OPERATION = 10021;
    public static final int RETURN = 10022;
    public static final int STATIC_RECEIVER = 10023;
    public static final int SWITCH = 10032;
    public static final int SWITCH_CASE = 10033;
    public static final int TERNARY_CONDITIONAL = 10024;
    public static final int THIS_REFERENCE = 10025;
    public static final int THROW = 10026;
    public static final int TRY = 10027;
    public static final int TYPE_COERCION = 10028;
    public static final int VARIABLE_REFERENCE = 10029;
    public static final int WHILE = 10030;
    public static final int FOR_EACH = 10034;
    public static final int STATEMENT_BLOCK = 10035;

    private NodeTypes() {
    }

    private static final Map<Class<? extends Node>, Integer> NODE_TYPES = ImmutableMap.<Class<? extends Node>, Integer>builder()
        .put(AnnotationNode.class, ANNOTATION)
        .put(ArrayNode.class, ARRAY)
        .put(AssignmentNode.class, ASSIGNMENT)
        .put(CastNode.class, CAST)
        .put(ClassNode.class, CLASS)
        .put(ConstructorCallNode.class, CONSTRUCTOR_CALL)
        .put(ConstructorNode.class, CONSTRUCTOR)
        .put(EnumNode.class, ENUM)
        //.put(ExceptionHandlerNode.class, EXCEPTION_HANDLER)
        .put(ExpressionStatementNode.class, EXPRESSION_STATEMENT)
        .put(FieldAccessNode.class, FIELD_ACCESS)
        .put(FieldDeclarationNode.class, FIELD_DECLARATION)
        .put(FormalArgumentNode.class, FORMAL_ARGUMENT)
        .put(FormalTypeParameterNode.class, FORMAL_TYPE_PARAMETER)
        .put(IfStatementNode.class, IF_STATEMENT)
        .put(InstanceOfNode.class, INSTANCE_OF)
        .put(InstanceReceiver.class, INSTANCE_RECEIVER)
        .put(InterfaceNode.class, INTERFACE)
        .put(LiteralNode.class, LITERAL)
        .put(LocalVariableDeclarationNode.class, LOCAL_VARIABLE_DECLARATION)
        .put(MethodCallNode.class, METHOD_CALL)
        .put(MethodNode.class, METHOD)
        .put(OperationNode.class, OPERATION)
        .put(ReturnNode.class, RETURN)
        .put(StaticReceiver.class, STATIC_RECEIVER)
        .put(SwitchNode.class, SWITCH)
        .put(SwitchCaseNode.class, SWITCH_CASE)
        .put(TernaryConditionalNode.class, TERNARY_CONDITIONAL)
        .put(ThisReferenceNode.class, THIS_REFERENCE)
        .put(ThrowNode.class, THROW)
        .put(TryNode.class, TRY)
        .put(TypeCoercionNode.class, TYPE_COERCION)
        .put(VariableReferenceNode.class, VARIABLE_REFERENCE)
        .put(WhileNode.class, WHILE)
        .put(ForEachNode.class, FOR_EACH)
        .put(StatementBlockNode.class, STATEMENT_BLOCK)
        .put(PrimitiveInstanceMethodCall.class, CsharpNodeTypes.PRIMITIVE_INSTANCE_METHOD_CALL)
        .put(PrimitiveStaticMethodCall.class, CsharpNodeTypes.PRIMITIVE_STATIC_METHOD_CALL)
        .build();

    public static int forClass(Class<?> nodeClass) {
        if (NODE_TYPES.containsKey(nodeClass)) {
            return NODE_TYPES.get(nodeClass);
        } else {
            throw new UnsupportedOperationException("Unknown node class: " + nodeClass);
        }
    }
}
