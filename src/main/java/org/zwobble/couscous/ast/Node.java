package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.NodeMapper;
import org.zwobble.couscous.ast.visitors.NodeVisitor;

public interface Node {
    default void accept(NodeVisitor visitor) {
        accept(new NodeMapper<Void>() {
            @Override
            public Void visit(LiteralNode literal) {
                visitor.visit(literal);
                return null;
            }

            @Override
            public Void visit(VariableReferenceNode variableReference) {
                visitor.visit(variableReference);
                return null;
            }

            @Override
            public Void visit(ThisReferenceNode reference) {
                visitor.visit(reference);
                return null;
            }

            @Override
            public Void visit(ArrayNode array) {
                visitor.visit(array);
                return null;
            }

            @Override
            public Void visit(AssignmentNode assignment) {
                visitor.visit(assignment);
                return null;
            }

            @Override
            public Void visit(TernaryConditionalNode ternaryConditional) {
                visitor.visit(ternaryConditional);
                return null;
            }

            @Override
            public Void visit(MethodCallNode methodCall) {
                visitor.visit(methodCall);
                return null;
            }

            @Override
            public Void visit(ConstructorCallNode call) {
                visitor.visit(call);
                return null;
            }

            @Override
            public Void visit(OperationNode operation) {
                visitor.visit(operation);
                return null;
            }

            @Override
            public Void visit(FieldAccessNode fieldAccess) {
                visitor.visit(fieldAccess);
                return null;
            }

            @Override
            public Void visit(TypeCoercionNode typeCoercion) {
                visitor.visit(typeCoercion);
                return null;
            }

            @Override
            public Void visit(CastNode cast) {
                visitor.visit(cast);
                return null;
            }

            @Override
            public Void visit(InstanceReceiver receiver) {
                visitor.visit(receiver);
                return null;
            }

            @Override
            public Void visit(StaticReceiver receiver) {
                visitor.visit(receiver);
                return null;
            }

            @Override
            public Void visit(ReturnNode returnNode) {
                visitor.visit(returnNode);
                return null;
            }

            @Override
            public Void visit(ThrowNode throwNode) {
                visitor.visit(throwNode);
                return null;
            }

            @Override
            public Void visit(ExpressionStatementNode expressionStatement) {
                visitor.visit(expressionStatement);
                return null;
            }

            @Override
            public Void visit(LocalVariableDeclarationNode localVariableDeclaration) {
                visitor.visit(localVariableDeclaration);
                return null;
            }

            @Override
            public Void visit(IfStatementNode ifStatement) {
                visitor.visit(ifStatement);
                return null;
            }

            @Override
            public Void visit(WhileNode whileLoop) {
                visitor.visit(whileLoop);
                return null;
            }

            @Override
            public Void visit(TryNode tryStatement) {
                visitor.visit(tryStatement);
                return null;
            }

            @Override
            public Void visit(FormalTypeParameterNode parameter) {
                visitor.visit(parameter);
                return null;
            }

            @Override
            public Void visit(FormalArgumentNode formalArgumentNode) {
                visitor.visit(formalArgumentNode);
                return null;
            }

            @Override
            public Void visit(AnnotationNode annotation) {
                visitor.visit(annotation);
                return null;
            }

            @Override
            public Void visit(MethodNode methodNode) {
                visitor.visit(methodNode);
                return null;
            }

            @Override
            public Void visit(ConstructorNode constructorNode) {
                visitor.visit(constructorNode);
                return null;
            }

            @Override
            public Void visit(FieldDeclarationNode declaration) {
                visitor.visit(declaration);
                return null;
            }

            @Override
            public Void visit(ClassNode classNode) {
                visitor.visit(classNode);
                return null;
            }

            @Override
            public Void visit(InterfaceNode interfaceNode) {
                visitor.visit(interfaceNode);
                return null;
            }

            @Override
            public Void visit(EnumNode enumNode) {
                visitor.visit(enumNode);
                return null;
            }
        });
    }
    <T> T accept(NodeMapper<T> visitor);
}
