package org.zwobble.couscous.backends.python;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.zwobble.couscous.ast.AssignmentNode;
import org.zwobble.couscous.ast.TypeName;
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
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeVisitorWithEmptyDefaults;
import org.zwobble.couscous.ast.visitors.NodeVisitors;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;
import org.zwobble.couscous.backends.python.ast.PythonBlock;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonImportNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonStatementNode;
import org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode;
import org.zwobble.couscous.values.BooleanValue;
import org.zwobble.couscous.values.IntegerValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.PrimitiveValueVisitor;
import org.zwobble.couscous.values.StringValue;
import org.zwobble.couscous.values.UnitValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;

import static com.google.common.collect.Iterators.singletonIterator;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.backends.python.ast.PythonAssignmentNode.pythonAssignment;
import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
import static org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode.pythonBooleanLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonClassNode.pythonClass;
import static org.zwobble.couscous.backends.python.ast.PythonConditionalExpressionNode.pythonConditionalExpression;
import static org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode.pythonFunctionDefinition;
import static org.zwobble.couscous.backends.python.ast.PythonImportNode.pythonImport;
import static org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode.pythonIntegerLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.util.ExtraLists.foldLeft;

import lombok.val;

public class PythonCodeGenerator {
    public static PythonModuleNode generateCode(ClassNode classNode) {
        val imports = generateImports(classNode).iterator();
        
        val pythonBody = classNode.getMethods()
            .stream()
            .map(PythonCodeGenerator::generateFunction)
            .collect(Collectors.toList());
        
        val pythonClass = pythonClass(classNode.getSimpleName(), pythonBody);
        
        return pythonModule(ImmutableList.copyOf(Iterators.concat(
            imports,
            singletonIterator(pythonClass))));
    }
    
    private static Stream<PythonImportNode> generateImports(ClassNode classNode) {
        val classes = findReferencedClasses(classNode);
        return classes.stream()
            .map(name -> pythonImport(name));
    }
    
    private static Set<TypeName> findReferencedClasses(ClassNode classNode) {
        val imports = ImmutableSet.<TypeName>builder();
        NodeVisitors.visitAll(classNode, new NodeVisitorWithEmptyDefaults() {
            @Override
            public void visit(StaticMethodCallNode staticMethodCall) {
                imports.add(staticMethodCall.getClassName());
            }
        });
        return imports.build();
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
                return pythonBooleanLiteral(value.getValue());
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
    
    private static class StatementGenerator implements StatementNodeMapper<PythonStatementNode> {
        @Override
        public PythonStatementNode visit(ReturnNode returnNode) {
            return pythonReturn(generateExpression(returnNode.getValue()));
        }

        @Override
        public PythonStatementNode visit(ExpressionStatementNode expressionStatement) {
            if (expressionStatement.getExpression() instanceof AssignmentNode) {
                val assignment = (AssignmentNode) expressionStatement.getExpression();
                return pythonAssignment(
                    EXPRESSION_GENERATOR.visit(assignment.getTarget()),
                    generateExpression(assignment.getValue()));
            } else {
                throw new UnsupportedOperationException();   
            }
        }

        @Override
        public PythonStatementNode visit(LocalVariableDeclarationNode declaration) {
            return pythonAssignment(
                pythonVariableReference(declaration.getDeclaration().getName()),
                generateExpression(declaration.getInitialValue()));
        }
    }
    
    private static PythonExpressionNode generateExpression(ExpressionNode expression) {
        return expression.accept(EXPRESSION_GENERATOR);
    }
    
    private static final ExpressionGenerator EXPRESSION_GENERATOR = new ExpressionGenerator();
    
    private static class ExpressionGenerator implements ExpressionNodeMapper<PythonExpressionNode> {
        @Override
        public PythonExpressionNode visit(LiteralNode literal) {
            return generateCode(literal.getValue());
        }

        @Override
        public PythonVariableReferenceNode visit(VariableReferenceNode variableReference) {
            return pythonVariableReference(variableReference.getReferent().getName());
        }

        @Override
        public PythonExpressionNode visit(AssignmentNode assignment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonExpressionNode visit(TernaryConditionalNode ternaryConditional) {
            return pythonConditionalExpression(
                generateExpression(ternaryConditional.getCondition()),
                generateExpression(ternaryConditional.getIfTrue()),
                generateExpression(ternaryConditional.getIfFalse()));
        }

        @Override
        public PythonExpressionNode visit(MethodCallNode methodCall) {
            val receiver = generateExpression(methodCall.getReceiver());
            val arguments = generateArguments(methodCall);
            if (isPrimitive(methodCall.getReceiver())) {
                val primitiveMethodGenerator = PrimitiveMethods.getPrimitiveMethod(
                    methodCall.getReceiver().getType(),
                    methodCall.getMethodName()).get();
                return primitiveMethodGenerator.generate(receiver, arguments);
            } else {
                return pythonCall(
                    pythonAttributeAccess(receiver, methodCall.getMethodName()),
                    arguments);
            }
        }

        @Override
        public PythonExpressionNode visit(StaticMethodCallNode staticMethodCall) {
            val className = staticMethodCall.getClassName();
            val moduleParts = asList(className.getQualifiedName().split(Pattern.quote(".")));
            
            val module = foldLeft(
                moduleParts,
                part -> (PythonExpressionNode)pythonVariableReference(part),
                (parent, part) -> pythonAttributeAccess(parent, part));
            
            val classReference = pythonAttributeAccess(
                module,
                className.getSimpleName());

            val methodReference = pythonAttributeAccess(
                classReference,
                staticMethodCall.getMethodName());
            
            val arguments = staticMethodCall.getArguments().stream()
                .map(PythonCodeGenerator::generateExpression)
                .collect(Collectors.toList());;
                
            return pythonCall(methodReference, arguments);
        }

        private List<PythonExpressionNode> generateArguments(MethodCallNode methodCall) {
            return methodCall.getArguments().stream()
                .map(PythonCodeGenerator::generateExpression)
                .collect(Collectors.toList());
        }
    }
    
    private static boolean isPrimitive(ExpressionNode value) {
        return PrimitiveMethods.isPrimitive(value.getType());
    }
}
