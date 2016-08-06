package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;

import java.util.HashMap;
import java.util.Map;

public class NodeTypes {
    private static int count = 0;
    private static final Map<Class<? extends Node>, Integer> NODE_TYPES = new HashMap<>();

    public static synchronized int register(Class<? extends Node> node) {
        NODE_TYPES.put(node, count);
        return count++;
    }

    public static final int ANNOTATION = register(AnnotationNode.class);
    public static final int ARRAY = register(ArrayNode.class);
    public static final int ASSIGNMENT = register(AssignmentNode.class);
    public static final int CAST = register(CastNode.class);
    public static final int CLASS = register(ClassNode.class);
    public static final int CONSTRUCTOR_CALL = register(ConstructorCallNode.class);
    public static final int CONSTRUCTOR = register(ConstructorNode.class);
    public static final int ENUM = register(EnumNode.class);
    public static final int EXPRESSION_STATEMENT = register(ExpressionStatementNode.class);
    public static final int FIELD_ACCESS = register(FieldAccessNode.class);
    public static final int FIELD_DECLARATION = register(FieldDeclarationNode.class);
    public static final int FORMAL_ARGUMENT = register(FormalArgumentNode.class);
    public static final int FORMAL_TYPE_PARAMETER = register(FormalTypeParameterNode.class);
    public static final int IF_STATEMENT = register(IfStatementNode.class);
    public static final int INSTANCE_OF = register(InstanceOfNode.class);
    public static final int INSTANCE_RECEIVER = register(InstanceReceiver.class);
    public static final int INTERFACE = register(InterfaceNode.class);
    public static final int LITERAL = register(LiteralNode.class);
    public static final int LOCAL_VARIABLE_DECLARATION = register(LocalVariableDeclarationNode.class);
    public static final int METHOD_CALL = register(MethodCallNode.class);
    public static final int METHOD = register(MethodNode.class);
    public static final int OPERATION = register(OperationNode.class);
    public static final int RETURN = register(ReturnNode.class);
    public static final int STATIC_RECEIVER = register(StaticReceiver.class);
    public static final int SWITCH = register(SwitchNode.class);
    public static final int SWITCH_CASE = register(SwitchCaseNode.class);
    public static final int TERNARY_CONDITIONAL = register(TernaryConditionalNode.class);
    public static final int THIS_REFERENCE = register(ThisReferenceNode.class);
    public static final int THROW = register(ThrowNode.class);
    public static final int TRY = register(TryNode.class);
    public static final int TYPE_COERCION = register(TypeCoercionNode.class);
    public static final int VARIABLE_REFERENCE = register(VariableReferenceNode.class);
    public static final int WHILE = register(WhileNode.class);
    public static final int FOR = register(ForNode.class);
    public static final int FOR_EACH = register(ForEachNode.class);
    public static final int STATEMENT_BLOCK = register(StatementBlockNode.class);

    private NodeTypes() {
    }

    public static int forClass(Class<?> nodeClass) {
        if (NODE_TYPES.containsKey(nodeClass)) {
            return NODE_TYPES.get(nodeClass);
        } else {
            try {
                Class.forName(nodeClass.getName(), true, nodeClass.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            if (NODE_TYPES.containsKey(nodeClass)) {
                return NODE_TYPES.get(nodeClass);
            } else {
                throw new UnsupportedOperationException("Unknown node class: " + nodeClass);
            }
        }
    }
}
