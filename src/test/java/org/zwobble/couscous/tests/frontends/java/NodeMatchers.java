package org.zwobble.couscous.tests.frontends.java;

import org.hamcrest.Matcher;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.types.Type;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.zwobble.couscous.tests.util.ExtraMatchers.hasFeature;
import static org.zwobble.couscous.tests.util.ExtraMatchers.isInstance;

public class NodeMatchers {
    public static Matcher<LocalVariableDeclarationNode> declarationHasValue(Matcher<ExpressionNode> matcher) {
        return hasFeature("initial value", node -> node.getInitialValue(), matcher);
    }

    public static Matcher<FormalTypeParameterNode> isFormalTypeParameter(String name) {
        return hasFeature("name", node -> node.getName(), equalTo(name));
    }

    public static Matcher<FormalArgumentNode> isFormalArgument(String name, Type type) {
        return allOf(
            hasFeature("name", node -> node.getName(), equalTo(name)),
            hasFeature("type", node -> node.getType(), equalTo(type)));
    }

    @SafeVarargs
    public static Matcher<ExpressionNode> isFieldAccess(Matcher<? super FieldAccessNode>... matchers) {
        return isInstance(FieldAccessNode.class, allOf(matchers));
    }

    public static Matcher<ExpressionNode> isVariableReference(String name, Type type) {
        return isInstance(VariableReferenceNode.class, allOf(
            hasFeature("variable name", node -> node.getReferent().getName(), equalTo(name)),
            expressionHasType(type)));
    }

    public static Matcher<FieldAccessNode> fieldHasReceiver(Matcher<ExpressionNode> receiverMatcher) {
        return hasFeature("receiver", FieldAccessNode::getLeft, isInstanceReceiver(receiverMatcher));
    }

    public static Matcher<Receiver> isInstanceReceiver(Matcher<ExpressionNode> receiverMatcher) {
        return isInstance(InstanceReceiver.class, hasFeature("value", receiver -> receiver.getExpression(), receiverMatcher));
    }

    public static Matcher<FieldAccessNode> fieldHasName(String name) {
        return hasFeature("field name", FieldAccessNode::getFieldName, equalTo(name));
    }

    public static Matcher<ExpressionNode> expressionHasType(Type type) {
        return hasFeature("type", ExpressionNode::getType, equalTo(type));
    }
}
