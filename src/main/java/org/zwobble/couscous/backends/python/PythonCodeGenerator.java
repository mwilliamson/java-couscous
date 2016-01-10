package org.zwobble.couscous.backends.python;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.structure.NodeStructure;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeMapperWithDefault;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;
import org.zwobble.couscous.backends.python.ast.*;
import org.zwobble.couscous.values.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.singletonIterator;
import static org.zwobble.couscous.backends.python.ast.PythonAssignmentNode.pythonAssignment;
import static org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode.pythonAttributeAccess;
import static org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode.pythonBooleanLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonCallNode.pythonCall;
import static org.zwobble.couscous.backends.python.ast.PythonClassNode.pythonClass;
import static org.zwobble.couscous.backends.python.ast.PythonConditionalExpressionNode.pythonConditionalExpression;
import static org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode.pythonFunctionDefinition;
import static org.zwobble.couscous.backends.python.ast.PythonIfStatementNode.pythonIfStatement;
import static org.zwobble.couscous.backends.python.ast.PythonImportAliasNode.pythonImportAlias;
import static org.zwobble.couscous.backends.python.ast.PythonImportNode.pythonImport;
import static org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode.pythonIntegerLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.backends.python.ast.PythonWhileNode.pythonWhile;
import static org.zwobble.couscous.util.ExtraLists.list;

public class PythonCodeGenerator {
    public static PythonModuleNode generateCode(ClassNode classNode) {
        Iterator<PythonImportNode> imports = generateImports(classNode).iterator();

        PythonImportNode internalsImport = pythonImport(
            importPathToRoot(classNode),
            list(pythonImportAlias("_couscous")));

        PythonFunctionDefinitionNode constructor =
            generateConstructor(classNode.getConstructor());

        Iterable<PythonFunctionDefinitionNode> pythonMethods = transform(
            classNode.getMethods(),
            PythonCodeGenerator::generateFunction);

        PythonClassNode pythonClass = pythonClass(
            classNode.getSimpleName(),
            ImmutableList.copyOf(Iterables.concat(list(constructor), pythonMethods)));

        return pythonModule(ImmutableList.copyOf(Iterators.concat(
            singletonIterator(pythonClass),
            imports,
            singletonIterator(internalsImport))));
    }

    private static Stream<PythonImportNode> generateImports(ClassNode classNode) {
        Set<TypeName> classes = findReferencedClasses(classNode);
        return classes.stream()
            .filter(name -> !name.equals(InternalCouscousValue.REF))
            .map(name -> pythonImport(importPathToRoot(classNode) + name.getQualifiedName(), list(pythonImportAlias(name.getSimpleName()))));
    }

    private static String importPathToRoot(ClassNode classNode) {
        return Strings.repeat(".", packageDepth(classNode) + 1);
    }

    private static int packageDepth(ClassNode classNode) {
        int depth = 0;
        final java.lang.String qualifiedName = classNode.getName().getQualifiedName();
        for (int index = 0; index < qualifiedName.length(); index++) {
            if (qualifiedName.charAt(index) == '.') {
                depth += 1;
            }
        }
        return depth;
    }

    private static Set<TypeName> findReferencedClasses(ClassNode classNode) {
        return NodeStructure.descendantNodes(classNode)
            .flatMap(node -> node.accept(new NodeMapperWithDefault<Stream<TypeName>>(Stream.empty()) {
                @Override
                public Stream<TypeName> visit(StaticReceiver receiver) {
                    return Stream.of(receiver.getType());
                }

                @Override
                public Stream<TypeName> visit(ConstructorCallNode call) {
                    return Stream.of(call.getType());
                }

                @Override
                public Stream<TypeName> visit(TypeCoercionNode typeCoercion) {
                    if (isIntegerBox(typeCoercion)) {
                        return Stream.of(ObjectValues.BOXED_INT);
                    } else {
                        return Stream.empty();
                    }
                }

                @Override
                public Stream<TypeName> visit(LiteralNode literal) {
                    if (literal.getValue() instanceof TypeValue) {
                        return Stream.of(((TypeValue)literal.getValue()).getValue());
                    } else {
                        return Stream.empty();
                    }
                }
            }))
            .collect(Collectors.toSet());
    }

    public static PythonExpressionNode generateCode(PrimitiveValue value) {
        return value.accept(new PrimitiveValueVisitor<PythonExpressionNode>(){

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

            @Override
            public PythonExpressionNode visit(TypeValue value) {
                return typeReference(value.getValue());
            }
        });
    }

    private static PythonFunctionDefinitionNode generateConstructor(ConstructorNode constructor) {
        Iterable<String> explicitArgumentNames = transform(constructor.getArguments(), argument -> argument.getName());
        Iterable<String> argumentNames = Iterables.concat(list("self"), explicitArgumentNames);
        List<PythonStatementNode> pythonBody = constructor.getBody().stream().map(PythonCodeGenerator::generateStatement).collect(Collectors.toList());
        return pythonFunctionDefinition("__init__", ImmutableList.copyOf(argumentNames), new PythonBlock(pythonBody));
    }

    private static PythonFunctionDefinitionNode generateFunction(MethodNode method) {
        Iterable<String> explicitArgumentNames = transform(method.getArguments(), argument -> argument.getName());
        Iterable<String> argumentNames = Iterables.concat(
            method.isStatic() ? Collections.<String>emptyList() : list("self"),
            explicitArgumentNames);
        List<PythonStatementNode> pythonBody = generateStatements(method.getBody());
        return pythonFunctionDefinition(toName(method.signature()), ImmutableList.copyOf(argumentNames), new PythonBlock(pythonBody));
    }

    private static List<PythonStatementNode> generateStatements(List<StatementNode> statements) {
        return statements.stream()
            .map(PythonCodeGenerator::generateStatement)
            .collect(Collectors.toList());
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
                final org.zwobble.couscous.ast.AssignmentNode assignment = (AssignmentNode)expressionStatement.getExpression();
                return pythonAssignment(assignment.getTarget().accept(EXPRESSION_GENERATOR), generateExpression(assignment.getValue()));
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public PythonStatementNode visit(LocalVariableDeclarationNode declaration) {
            return pythonAssignment(pythonVariableReference(declaration.getDeclaration().getName()), generateExpression(declaration.getInitialValue()));
        }

        @Override
        public PythonStatementNode visit(IfStatementNode ifStatement) {
            return pythonIfStatement(
                generateExpression(ifStatement.getCondition()),
                generateStatements(ifStatement.getTrueBranch()),
                generateStatements(ifStatement.getFalseBranch()));
        }

        @Override
        public PythonStatementNode visit(WhileNode whileLoop) {
            return pythonWhile(
                generateExpression(whileLoop.getCondition()),
                generateStatements(whileLoop.getBody()));
        }
    }

    private static PythonExpressionNode generateExpression(ExpressionNode expression) {
        return expression.accept(EXPRESSION_GENERATOR);
    }

    private static List<PythonExpressionNode> generateExpressions(Iterable<? extends ExpressionNode> expressions) {
        return ImmutableList.copyOf(transform(expressions, PythonCodeGenerator::generateExpression));
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
        public PythonExpressionNode visit(ThisReferenceNode reference) {
            return pythonVariableReference("self");
        }

        @Override
        public PythonExpressionNode visit(AssignmentNode assignment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public PythonExpressionNode visit(TernaryConditionalNode ternaryConditional) {
            return pythonConditionalExpression(generateExpression(ternaryConditional.getCondition()), generateExpression(ternaryConditional.getIfTrue()), generateExpression(ternaryConditional.getIfFalse()));
        }

        @Override
        public PythonExpressionNode visit(MethodCallNode methodCall) {
            PythonExpressionNode receiver = generateReceiver(methodCall.getReceiver());
            List<PythonExpressionNode> arguments = generateExpressions(methodCall.getArguments());

            return getPrimitiveMethod(methodCall, receiver, arguments)
                .orElseGet(() -> pythonCall(pythonAttributeAccess(receiver, toName(methodCall.signature())), arguments));
        }

        private Optional<PythonExpressionNode> getPrimitiveMethod(
            MethodCallNode methodCall,
            PythonExpressionNode pythonReceiver,
            List<PythonExpressionNode> pythonArguments
        ) {
            return methodCall.getReceiver().accept(new Receiver.Mapper<Optional<PythonExpressionNode>>() {
                @Override
                public Optional<PythonExpressionNode> visit(ExpressionNode receiver) {
                    return PrimitiveMethods.getPrimitiveMethod(receiver.getType(), methodCall.getMethodName())
                        .map(generator -> generator.generate(pythonReceiver, pythonArguments));
                }

                @Override
                public Optional<PythonExpressionNode> visit(TypeName receiver) {
                    return PrimitiveMethods.getPrimitiveStaticMethod(receiver, methodCall.getMethodName())
                        .map(generator -> generator.generate(pythonArguments));
                }
            });
        }

        @Override
        public PythonExpressionNode visit(ConstructorCallNode call) {
            final org.zwobble.couscous.ast.TypeName className = call.getType();
            final org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode classReference = typeReference(className);
            final java.util.List<org.zwobble.couscous.backends.python.ast.PythonExpressionNode> arguments = generateExpressions(call.getArguments());
            return pythonCall(classReference, arguments);
        }

        @Override
        public PythonExpressionNode visit(FieldAccessNode fieldAccess) {
            return pythonAttributeAccess(generateExpression(fieldAccess.getLeft()), fieldAccess.getFieldName());
        }

        @Override
        public PythonExpressionNode visit(TypeCoercionNode typeCoercion) {
            PythonExpressionNode value = generateExpression(typeCoercion.getExpression());
            if (isIntegerBox(typeCoercion)) {
                // TODO: perform this replacement at the start so that we don't split the import handling and actual expression
                return visit(ConstructorCallNode.constructorCall(
                    ObjectValues.BOXED_INT,
                    list(typeCoercion.getExpression())));
            } else if (isIntegerUnbox(typeCoercion)) {
                return pythonAttributeAccess(value, "_value");
            } else {
                return value;
            }
        }
    }

    private static PythonExpressionNode generateReceiver(Receiver receiver) {
        return receiver.accept(new Receiver.Mapper<PythonExpressionNode>() {
            @Override
            public PythonExpressionNode visit(ExpressionNode receiver) {
                return generateExpression(receiver);
            }

            @Override
            public PythonExpressionNode visit(TypeName receiver) {
                return typeReference(receiver);
            }
        });
    }

    private static boolean isIntegerBox(TypeCoercionNode typeCoercion) {
        return isInteger(typeCoercion.getExpression().getType()) && !isInteger(typeCoercion.getType());
    }

    private static boolean isIntegerUnbox(TypeCoercionNode typeCoercion) {
        return !isInteger(typeCoercion.getExpression().getType()) && isInteger(typeCoercion.getType());
    }

    private static boolean isInteger(TypeName type) {
        return type.equals(IntegerValue.REF) || type.equals(ObjectValues.BOXED_INT);
    }

    private static PythonVariableReferenceNode typeReference(TypeName className) {
        return pythonVariableReference(className.getSimpleName());
    }

    public static String toName(MethodSignature signature) {
        return Joiner.on("__").join(Iterables.concat(
            list(signature.getName()),
            transform(signature.getArguments(), argument -> argument.getQualifiedName().replace('.', '_'))));
    }
}