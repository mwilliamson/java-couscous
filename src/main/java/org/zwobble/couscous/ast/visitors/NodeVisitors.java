package org.zwobble.couscous.ast.visitors;

import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ConstructorCallNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.Node;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.VariableReferenceNode;

import lombok.val;

public class NodeVisitors {
    public static void visitAll(Node node, NodeVisitor visitor) {
        node.accept(new NodeVisitor() {
            @Override
            public void visit(LiteralNode literal) {
                visitor.visit(literal);
            }
            
            @Override
            public void visit(VariableReferenceNode variableReference) {
                visitor.visit(variableReference);
            }
            
            @Override
            public void visit(AssignmentNode assignment) {
                visitor.visit(assignment);
                assignment.getValue().accept(this);
                visit(assignment.getTarget());
            }
            
            @Override
            public void visit(TernaryConditionalNode ternaryConditional) {
                visitor.visit(ternaryConditional);
                ternaryConditional.getCondition().accept(this);
                ternaryConditional.getIfTrue().accept(this);
                ternaryConditional.getIfFalse().accept(this);
            }
            
            @Override
            public void visit(MethodCallNode methodCall) {
                visitor.visit(methodCall);
                methodCall.getReceiver().accept(this);
                for (val argument : methodCall.getArguments()) {
                    argument.accept(this);
                }
            }
            
            @Override
            public void visit(StaticMethodCallNode staticMethodCall) {
                visitor.visit(staticMethodCall);
                for (val argument : staticMethodCall.getArguments()) {
                    argument.accept(this);
                }
            }

            @Override
            public void visit(ConstructorCallNode call) {
                visitor.visit(call);
                for (val argument : call.getArguments()) {
                    argument.accept(this);
                }
            }
            
            @Override
            public void visit(ReturnNode returnNode) {
                visitor.visit(returnNode);
                returnNode.getValue().accept(this);
            }
            
            @Override
            public void visit(ExpressionStatementNode expressionStatement) {
                visitor.visit(expressionStatement);
                expressionStatement.getExpression().accept(this);
            }
            
            @Override
            public void visit(LocalVariableDeclarationNode localVariableDeclaration) {
                visitor.visit(localVariableDeclaration);
                localVariableDeclaration.getInitialValue().accept(this);
            }

            @Override
            public void visit(ClassNode classNode) {
                visitor.visit(classNode);
                for (val method : classNode.getMethods()) {
                    method.accept(this);
                }
            }

            @Override
            public void visit(MethodNode methodNode) {
                for (val statement : methodNode.getBody()) {
                    statement.accept(this);
                }
            }
        });
    }
}
