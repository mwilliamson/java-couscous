package org.zwobble.couscous.tests.frontends.java;

import org.hamcrest.Matcher;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.sugar.SwitchCaseNode;
import org.zwobble.couscous.ast.sugar.SwitchNode;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.*;
import static org.zwobble.couscous.tests.util.ExtraMatchers.hasFeature;
import static org.zwobble.couscous.tests.util.ExtraMatchers.isInstance;

public class NodeMatchers {
    public static Matcher<LocalVariableDeclarationNode> declarationHasValue(Matcher<ExpressionNode> matcher) {
        return hasFeature("initial value", node -> node.getInitialValue(), matcher);
    }

    public static Matcher<FormalTypeParameterNode> isFormalTypeParameter(Identifier declaringScope, String name) {
        return allOf(
            hasFeature("name", node -> node.getType().getName(), equalTo(name)),
            hasFeature("declaringScope", node -> node.getType().getDeclaringScope(), equalTo(declaringScope)));
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

    @SafeVarargs
    public static Matcher<StatementNode> isLocalVariableDeclaration(Matcher<? super LocalVariableDeclarationNode>... matchers) {
        return isInstance(LocalVariableDeclarationNode.class, allOf(matchers));
    }

    @SafeVarargs
    public static Matcher<StatementNode> isTryStatement(Matcher<? super TryNode>... matchers) {
        return isInstance(TryNode.class, allOf(matchers));
    }

    public static Matcher<TryNode> hasTryBody(List<StatementNode> statements) {
        return hasFeature("body", TryNode::getBody, equalTo(statements));
    }

    @SafeVarargs
    public static Matcher<TryNode> hasExceptionHandlers(Matcher<ExceptionHandlerNode>... handlers) {
        return hasFeature("exception handlers", TryNode::getExceptionHandlers, contains(handlers));
    }

    public static Matcher<ExceptionHandlerNode> isExceptionHandler(Type type, String name, List<StatementNode> body) {
        return allOf(
            hasFeature("exception type", ExceptionHandlerNode::getExceptionType, equalTo(type)),
            hasFeature("exception name", ExceptionHandlerNode::getExceptionName, equalTo(name)),
            hasFeature("body", ExceptionHandlerNode::getBody, equalTo(body)));
    }

    public static Matcher<TryNode> hasFinally(StatementNode... statements) {
        Matcher<Iterable<? extends StatementNode>> statementsMatcher = statements.length == 0
            ? emptyIterable()
            : contains(statements);
        return hasFeature("finally clause", TryNode::getFinallyBody, statementsMatcher);
    }

    @SafeVarargs
    public static Matcher<StatementNode> isSwitch(Matcher<SwitchNode>... matchers) {
        return isInstance(SwitchNode.class, allOf(matchers));
    }

    public static Matcher<SwitchNode> hasSwitchValue(Matcher<ExpressionNode> matcher) {
        return hasFeature("switch value", SwitchNode::getValue, matcher);
    }

    @SafeVarargs
    public static Matcher<SwitchNode> hasSwitchCases(Matcher<SwitchCaseNode>... matchers) {
        return hasFeature("switch cases", SwitchNode::getCases, contains(asList(matchers)));
    }

    public static Matcher<SwitchCaseNode> isCase(ExpressionNode value, List<StatementNode> statements) {
        return isCase(Optional.of(value), statements);
    }

    public static Matcher<SwitchCaseNode> isDefaultCase(List<StatementNode> statements) {
        return isCase(Optional.empty(), statements);
    }

    private static Matcher<SwitchCaseNode> isCase(Optional<ExpressionNode> value, List<StatementNode> statements) {
        return allOf(
            hasFeature("value", SwitchCaseNode::getValue, equalTo(value)),
            hasFeature("statements", SwitchCaseNode::getStatements, equalTo(statements))
        );
    }
}
