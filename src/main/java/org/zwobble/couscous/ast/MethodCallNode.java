package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeTransformer;

import java.util.List;

import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

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
        MethodSignature signature)
    {
        return staticMethodCall(ScalarType.of(className), methodName, arguments, signature);
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
        MethodSignature signature)
    {
        return methodCall(staticReceiver(className), methodName, arguments, signature);
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
        MethodSignature signature)
    {
        return methodCall(instanceReceiver(receiver), methodName, arguments, signature);
    }

    public static MethodCallNode methodCall(
        Receiver receiver,
        String methodName,
        List<ExpressionNode> arguments,
        Type type)
    {
        MethodSignature signature = new MethodSignature(
            methodName,
            eagerMap(arguments, argument -> argument.getType()),
            type);
        return methodCall(receiver, methodName, arguments, signature);
    }

    public static MethodCallNode methodCall(
        Receiver receiver,
        String methodName,
        List<ExpressionNode> arguments,
        MethodSignature signature)
    {
        return new MethodCallNode(receiver, methodName, arguments, signature);
    }
    
    private final Receiver receiver;
    private final String methodName;
    private final List<ExpressionNode> arguments;
    private final MethodSignature signature;
    
    private MethodCallNode(
        Receiver receiver,
        String methodName,
        List<ExpressionNode> arguments,
        MethodSignature signature)
    {
        this.receiver = receiver;
        this.methodName = methodName;
        this.arguments = arguments;
        this.signature = signature;
    }
    
    public Receiver getReceiver() {
        return receiver;
    }
    
    public String getMethodName() {
        return methodName;
    }
    
    public List<ExpressionNode> getArguments() {
        return arguments;
    }

    @Override
    public Type getType() {
        return signature.getReturnType();
    }

    public MethodSignature signature() {
        return signature;
    }
    
    @Override
    public <T> T accept(ExpressionNodeMapper<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public ExpressionNode transform(NodeTransformer transformer) {
        return new MethodCallNode(
            transformer.transformReceiver(receiver),
            transformer.transformMethodName(signature()),
            transformer.transformExpressions(arguments),
            transformer.transform(signature));
    }

    @Override
    public String toString() {
        return "MethodCallNode(receiver=" + receiver + ", methodName="
               + methodName + ", arguments=" + arguments + ", signature=" + signature
               + ")";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                 + ((arguments == null) ? 0 : arguments.hashCode());
        result = prime * result
                 + ((methodName == null) ? 0 : methodName.hashCode());
        result = prime * result
                 + ((receiver == null) ? 0 : receiver.hashCode());
        result = prime * result + ((signature == null) ? 0 : signature.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MethodCallNode other = (MethodCallNode) obj;
        if (arguments == null) {
            if (other.arguments != null)
                return false;
        } else if (!arguments.equals(other.arguments))
            return false;
        if (methodName == null) {
            if (other.methodName != null)
                return false;
        } else if (!methodName.equals(other.methodName))
            return false;
        if (receiver == null) {
            if (other.receiver != null)
                return false;
        } else if (!receiver.equals(other.receiver))
            return false;
        if (signature == null) {
            if (other.signature != null)
                return false;
        } else if (!signature.equals(other.signature))
            return false;
        return true;
    }
}
