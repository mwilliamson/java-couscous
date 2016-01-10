package org.zwobble.couscous.ast;

import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.InternalCouscousValue;
import org.zwobble.couscous.values.ObjectValues;

import java.util.List;

import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.util.ExtraLists.list;

public class StaticMethodCallNode {
    public static ExpressionNode same(ExpressionNode left, ExpressionNode right) {
        return staticMethodCall(InternalCouscousValue.REF, "same", list(left, right), BooleanValue.REF);
    }
    
    public static ExpressionNode boxInt(ExpressionNode value) {
        return typeCoercion(value, ObjectValues.BOXED_INT);
    }

    public static ExpressionNode unboxInt(ExpressionNode value) {
        return typeCoercion(value, IntegerValue.REF);
    }
    
    public static ExpressionNode staticMethodCall(
            String className,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return staticMethodCall(TypeName.of(className), methodName, arguments, type);
    }
    
    public static ExpressionNode staticMethodCall(
            TypeName className,
            String methodName,
            List<ExpressionNode> arguments,
            TypeName type) {
        return MethodCallNode.methodCall(staticReceiver(className), methodName, arguments, type);
    }
}
