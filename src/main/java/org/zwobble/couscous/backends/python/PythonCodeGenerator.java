package org.zwobble.couscous.backends.python;

import java.util.stream.Collectors;

import org.zwobble.couscous.ast.Assignment;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ExpressionNode;
import org.zwobble.couscous.ast.ExpressionStatementNode;
import org.zwobble.couscous.ast.LiteralNode;
import org.zwobble.couscous.ast.LocalVariableDeclarationNode;
import org.zwobble.couscous.ast.MethodCallNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.ReturnNode;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.StaticMethodCallNode;
import org.zwobble.couscous.ast.TernaryConditionalNode;
import org.zwobble.couscous.ast.VariableReferenceNode;
import org.zwobble.couscous.ast.visitors.ExpressionNodeVisitor;
import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;
import org.zwobble.couscous.backends.python.ast.PythonBlock;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonStatementNode;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValueVisitor;
import org.zwobble.couscous.values.StringValue;
import org.zwobble.couscous.values.UnitValue;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.ast.PythonClassNode.pythonClass;
import static org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode.pythonFunctionDefinition;
import static org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode.pythonIntegerLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;

import lombok.val;

public class PythonCodeGenerator {
    public static PythonModuleNode generateCode(ClassNode classNode) {
        val pythonBody = classNode.getMethods()
            .stream()
            .map(PythonCodeGenerator::generateFunction)
            .collect(Collectors.toList());
        
        val pythonClass = pythonClass(classNode.getLocalName(), pythonBody);
        return pythonModule(asList(pythonClass));
    }
    
    public static PythonExpressionNode generateCode(PrimitiveValue value) {
        return value.accept(new PrimitiveValueVisitor<PythonExpressionNode>() {
            @Override
            public PythonExpressionNode visit(IntegerValue value) {
                return pythonIntegerLiteral(value.getValue());
            }

            @Override
            public PythonExpressionNode visit(StringValue value) {
                return pythonStringLiteral(value.getValue());
            }

            @Override
            public PythonExpressionNode visit(BooleanValue value) {
                throw new UnsupportedOperationException();
            }

            @Override
            public PythonExpressionNode visit(UnitValue unitValue) {
                throw new UnsupportedOperationException();
            }
        });
    }
    
    private static PythonFunctionDefinitionNode generateFunction(MethodNode method) {
        val argumentNames = method.getArguments().stream()
            .map(argument -> argument.getName())
            .collect(Collectors.toList());
            
        val pythonBody = method.getBody()
            .stream()
            .map(PythonCodeGenerator::generateStatement)
            .collect(Collectors.toList());
        
        return pythonFunctionDefinition(method.getName(), argumentNames, new PythonBlock(pythonBody));
    }
    
    private static PythonStatementNode generateStatement(StatementNode statement) {
        return statement.accept(new StatementGenerator());
    }
    
    private static class StatementGenerator implements StatementNodeVisitor<PythonStatementNode> {
        @Override
        public PythonStatementNode visit(ReturnNode returnNode) {
            return pythonReturn(generateExpression(returnNode.getValue()));
        }

        @Override
        public PythonStatementNode visit(ExpressionStatementNode expressionStatement) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonStatementNode visit(LocalVariableDeclarationNode localVariableDeclaration) {
            throw new UnsupportedOperationException();
        }
    }
    
    private static PythonExpressionNode generateExpression(ExpressionNode expression) {
        return expression.accept(new ExpressionGenerator());
    }
    
    private static class ExpressionGenerator implements ExpressionNodeVisitor<PythonExpressionNode> {
        @Override
        public PythonExpressionNode visit(LiteralNode literal) {
            return generateCode(literal.getValue());
        }

        @Override
        public PythonExpressionNode visit(VariableReferenceNode variableReference) {
            return pythonVariableReference(variableReference.getReferent().getName());
        }

        @Override
        public PythonExpressionNode visit(Assignment assignment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonExpressionNode visit(TernaryConditionalNode ternaryConditional) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonExpressionNode visit(MethodCallNode methodCall) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonExpressionNode visit(StaticMethodCallNode staticMethodCall) {
            throw new UnsupportedOperationException();
        }
        
    }
}
