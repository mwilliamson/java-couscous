package org.zwobble.couscous.ast.visitors;

import java.util.List;

import org.zwobble.couscous.ast.*;

public class NodeVisitors {
    public static void visitAll(Node node, NodeVisitor visitor) {
        node.accept(new NodeVisitor(){
            @Override
            public void visit(LiteralNode literal) {
                visitor.visit(literal);
            }
            
            @Override
            public void visit(VariableReferenceNode variableReference) {
                visitor.visit(variableReference);
            }
            
            @Override
            public void visit(ThisReferenceNode reference) {
                visitor.visit(reference);
            }
            
            @Override
            public void visit(AssignmentNode assignment) {
                visitor.visit(assignment);
                assignment.getValue().accept(this);
                assignment.getTarget().accept(this);
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
                visitAll(methodCall.getArguments());
            }
            
            @Override
            public void visit(StaticMethodCallNode staticMethodCall) {
                visitor.visit(staticMethodCall);
                visitAll(staticMethodCall.getArguments());
            }
            
            @Override
            public void visit(ConstructorCallNode call) {
                visitor.visit(call);
                visitAll(call.getArguments());
            }
            
            @Override
            public void visit(FieldAccessNode fieldAccess) {
                visitor.visit(fieldAccess);
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
            public void visit(IfStatementNode ifStatement) {
                ifStatement.getCondition().accept(visitor);
                visitAll(ifStatement.getTrueBranch());
                visitAll(ifStatement.getFalseBranch());
            }

            @Override
            public void visit(WhileNode whileLoop) {
                whileLoop.getCondition().accept(visitor);
                visitAll(whileLoop.getBody());
            }

            @Override
            public void visit(ClassNode classNode) {
                visitor.visit(classNode);
                classNode.getConstructor().accept(this);
                visitAll(classNode.getMethods());
            }
            
            @Override
            public void visit(MethodNode methodNode) {
                visitAll(methodNode.getBody());
            }
            
            @Override
            public void visit(ConstructorNode constructorNode) {
                visitAll(constructorNode.getBody());
            }
            
            private <T extends Node> void visitAll(List<T> nodes) {
                for (Node node : nodes) {
                    node.accept(this);
                }
            }
        });
    }
}