package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.List;

import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;
import static org.zwobble.couscous.util.ExtraLists.list;

public class MethodCallNode implements ExpressionNode {
    public static ExpressionNode staticMethodCall(
        String className,
        String methodName,
        List<ExpressionNode> arguments,
        Type type)
    {
        return staticMethodCall(ScalarType.of(className), methodName, arguments, type);
    }

    public static ExpressionNode staticMethodCall(
        String className,
        String methodName,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        return staticMethodCall(ScalarType.of(className), methodName, arguments, returnType, signature);
    }

    public static ExpressionNode staticMethodCall(
        ScalarType className,
        String methodName,
        List<ExpressionNode> arguments,
        Type type)
    {
        return methodCall(staticReceiver(className), methodName, arguments, type);
    }

    public static ExpressionNode staticMethodCall(
        ScalarType className,
        String methodName,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        return methodCall(staticReceiver(className), methodName, arguments, returnType, signature);
    }

    public static MethodCallNode methodCall(
        ExpressionNode receiver,
        String methodName,
        List<ExpressionNode> arguments,
        Type type)
    {
        return methodCall(instanceReceiver(receiver), methodName, arguments, type);
    }

    public static MethodCallNode methodCall(
        ExpressionNode receiver,
        String methodName,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        return methodCall(instanceReceiver(receiver), methodName, arguments, returnType, signature);
    }

    public static MethodCallNode methodCall(
        Receiver receiver,
        String methodName,
        List<ExpressionNode> arguments,
        Type type)
    {
        MethodSignature signature = new MethodSignature(
            methodName,
            list(),
            eagerMap(arguments, argument -> argument.getType()),
            type);
        return methodCall(receiver, methodName, arguments, type, signature);
    }

    public static MethodCallNode methodCall(
        Receiver receiver,
        String methodName,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        return methodCall(receiver, methodName, list(), arguments, returnType, signature);
    }

    public static MethodCallNode methodCall(
        Receiver receiver,
        String methodName,
        List<Type> typeParameters,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        return new MethodCallNode(receiver, methodName, typeParameters, arguments, returnType, signature);
    }
    
    private final Receiver receiver;
    private final String methodName;
    private final List<Type> typeParameters;
    private final List<ExpressionNode> arguments;
    private final Type returnType;
    private final MethodSignature signature;
    
    private MethodCallNode(
        Receiver receiver,
        String methodName,
        List<Type> typeParameters,
        List<ExpressionNode> arguments,
        Type returnType,
        MethodSignature signature)
    {
        this.receiver = receiver;
        this.methodName = methodName;
        this.typeParameters = typeParameters;
        this.arguments = arguments;
        this.returnType = returnType;
        this.signature = signature;
    }
    
    public Receiver getReceiver() {
        return receiver;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public List<Type> getTypeParameters() {
        return typeParameters;
    }
    
    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public Type getType() {
        return returnType;
    }

    public MethodSignature signature() {
        return signature;
    }
    
    @Override
    public int nodeType() {
        return NodeTypes.METHOD_CALL;
    }

    @Override
    public Iterable<? extends Node> childNodes() {
        return ExtraIterables.lazyCons(receiver, arguments);
    }

    @Override
    public ExpressionNode transformSubtree(NodeTransformer transformer) {
        return new MethodCallNode(
            transformer.transformReceiver(receiver),
            transformer.transformMethodName(signature()),
            eagerMap(typeParameters, transformer::transform),
            transformer.transformExpressions(arguments),
            transformer.transform(returnType),
            transformer.transform(signature));
    }

    @Override
    public String toString() {
        return "MethodCallNode(" +
            "receiver=" + receiver +
            ", methodName=" + methodName +
            ", typeParameters=" + typeParameters +
            ", arguments=" + arguments +
            ", returnType=" + returnType +
            ", signature=" + signature +
            ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodCallNode that = (MethodCallNode) o;

        if (!receiver.equals(that.receiver)) return false;
        if (!methodName.equals(that.methodName)) return false;
        if (!typeParameters.equals(that.typeParameters)) return false;
        if (!arguments.equals(that.arguments)) return false;
        if (!returnType.equals(that.returnType)) return false;
        return signature.equals(that.signature);

    }

    @Override
    public int hashCode() {
        int result = receiver.hashCode();
        result = 31 * result + methodName.hashCode();
        result = 31 * result + typeParameters.hashCode();
        result = 31 * result + arguments.hashCode();
        result = 31 * result + returnType.hashCode();
        result = 31 * result + signature.hashCode();
        return result;
    }
}
