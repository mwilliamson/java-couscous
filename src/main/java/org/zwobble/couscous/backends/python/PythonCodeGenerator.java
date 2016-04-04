package org.zwobble.couscous.backends.python;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.structure.NodeStructure;
import org.zwobble.couscous.ast.visitors.ExpressionNodeMapper;
import org.zwobble.couscous.ast.visitors.NodeMapperWithDefault;
import org.zwobble.couscous.ast.visitors.StatementNodeMapper;
import org.zwobble.couscous.backends.naming.Names;
import org.zwobble.couscous.backends.python.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.InternalCouscousValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.TypeValue;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Iterators.singletonIterator;
import static java.util.Collections.emptyList;
import static org.zwobble.couscous.ast.ExpressionStatementNode.expressionStatement;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.TypeCoercionNode.typeCoercion;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
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
import static org.zwobble.couscous.backends.python.ast.PythonListNode.pythonList;
import static org.zwobble.couscous.backends.python.ast.PythonModuleNode.pythonModule;
import static org.zwobble.couscous.backends.python.ast.PythonReturnNode.pythonReturn;
import static org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode.pythonStringLiteral;
import static org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode.pythonVariableReference;
import static org.zwobble.couscous.backends.python.ast.PythonWhileNode.pythonWhile;
import static org.zwobble.couscous.backends.python.ast.visitors.PythonExpressionStatement.pythonExpressionStatement;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.*;
import static org.zwobble.couscous.util.ExtraLists.*;

public class PythonCodeGenerator {
    public static PythonModuleNode generateCode(TypeNode typeNode) {
        Iterator<PythonImportNode> imports = generateImports(typeNode).iterator();

        PythonImportNode internalsImport = pythonImport(
            importPathToRoot(typeNode),
            list(pythonImportAlias("_couscous")));

        Optional<ClassNode> classNode = tryCast(ClassNode.class, typeNode);
        List<? extends PythonStatementNode> constructor = classNode
            .map(node -> list(generateConstructor(node.getConstructor())))
            .orElse(list());

        List<PythonStatementNode> staticConstructor = classNode
            .map(node -> generateStatements(node.getStaticConstructor()))
            .orElse(list());

        Iterable<PythonFunctionDefinitionNode> pythonMethods = lazyFlatMap(
            filter(typeNode.getMethods(), method -> !method.isAbstract()),
            method -> generateMethods(typeNode.getName(), method));

        PythonClassNode pythonClass = pythonClass(
            typeNode.getName().getSimpleName(),
            ImmutableList.copyOf(Iterables.concat(constructor, pythonMethods)));

        return pythonModule(ImmutableList.copyOf(Iterators.concat(
            singletonIterator(pythonClass),
            imports,
            singletonIterator(internalsImport),
            staticConstructor.iterator())));
    }

    private static Stream<PythonImportNode> generateImports(TypeNode classNode) {
        Set<ScalarType> classes = findReferencedClasses(classNode);
        return classes.stream()
            .filter(name -> !name.equals(InternalCouscousValue.REF))
            .map(name -> pythonImport(importPathToRoot(classNode) + name.getQualifiedName(), list(pythonImportAlias(name.getSimpleName()))));
    }

    private static String importPathToRoot(TypeNode classNode) {
        return Strings.repeat(".", packageDepth(classNode) + 1);
    }

    private static int packageDepth(TypeNode classNode) {
        int depth = 0;
        final java.lang.String qualifiedName = classNode.getName().getQualifiedName();
        for (int index = 0; index < qualifiedName.length(); index++) {
            if (qualifiedName.charAt(index) == '.') {
                depth += 1;
            }
        }
        return depth;
    }

    private static Set<ScalarType> findReferencedClasses(TypeNode classNode) {
        return NodeStructure.descendantNodes(classNode)
            .flatMap(node -> node.accept(new NodeMapperWithDefault<Stream<ScalarType>>(Stream.empty()) {
                @Override
                public Stream<ScalarType> visit(StaticReceiver receiver) {
                    return Stream.of(receiver.getType());
                }

                @Override
                public Stream<ScalarType> visit(ConstructorCallNode call) {
                    return Stream.of(erasure(call.getType()));
                }

                @Override
                public Stream<ScalarType> visit(TypeCoercionNode typeCoercion) {
                    if (isIntegerBox(typeCoercion)) {
                        return Stream.of(Types.BOXED_INT);
                    } else {
                        return Stream.empty();
                    }
                }

                @Override
                public Stream<ScalarType> visit(LiteralNode literal) {
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
        return value.accept(new PrimitiveValue.Visitor<PythonExpressionNode>(){

            @Override
            public PythonExpressionNode visitInteger(int value) {
                return pythonIntegerLiteral(value);
            }

            @Override
            public PythonExpressionNode visitString(String value) {
                return pythonStringLiteral(value);
            }

            @Override
            public PythonExpressionNode visitBoolean(boolean value) {
                return pythonBooleanLiteral(value);
            }

            @Override
            public PythonExpressionNode visitUnit() {
                throw new UnsupportedOperationException();
            }

            @Override
            public PythonExpressionNode visitType(ScalarType value) {
                return typeReference(value);
            }
        });
    }

    private static PythonFunctionDefinitionNode generateConstructor(ConstructorNode constructor) {
        Iterable<String> explicitArgumentNames = transform(constructor.getArguments(), argument -> argument.getName());
        Iterable<String> argumentNames = Iterables.concat(list("self"), explicitArgumentNames);
        List<PythonStatementNode> pythonBody = constructor.getBody().stream().map(PythonCodeGenerator::generateStatement).collect(Collectors.toList());
        return pythonFunctionDefinition("__init__", ImmutableList.copyOf(argumentNames), new PythonBlock(pythonBody));
    }

    private static Iterable<PythonFunctionDefinitionNode> generateMethods(ScalarType type, MethodNode method) {
        return lazyMap(
            lazyCons(method, generateOverrideMethods(type, method)),
            PythonCodeGenerator::generateFunction);
    }

    private static Iterable<MethodNode> generateOverrideMethods(ScalarType type, MethodNode methodNode) {
        return transform(
            methodNode.getOverrides(),
            override -> {
                List<FormalArgumentNode> arguments = eagerMapWithIndex(
                    override.getArguments(),
                    (argument, index) -> formalArg(var(null, "arg" + index, argument)));
                MethodCallNode call = MethodCallNode.methodCall(
                    thisReference(type),
                    methodNode.getName(),
                    eagerMap(arguments, argument -> reference(argument)),
                    methodNode.signature());

                StatementNode body = override.getReturnType().equals(Types.VOID)
                    ? expressionStatement(call)
                    : returns(typeCoercion(call, override.getReturnType()));

                return MethodNode.method(
                    methodNode.getAnnotations(),
                    methodNode.isStatic(),
                    methodNode.getName(),
                    // TODO: should get type parameters from signature
                    list(),
                    arguments,
                    override.getReturnType(),
                    Optional.of(list(body)),
                    emptyList());
            });
    }

    private static PythonFunctionDefinitionNode generateFunction(MethodNode method) {
        Iterable<String> explicitArgumentNames = transform(method.getArguments(), argument -> argument.getName());
        Iterable<String> argumentNames = Iterables.concat(
            method.isStatic() ? Collections.<String>emptyList() : list("self"),
            explicitArgumentNames);
        List<PythonStatementNode> pythonBody = generateStatements(method.getBody().get());
        return pythonFunctionDefinition(Names.toUniqueName(method.signature()), ImmutableList.copyOf(argumentNames), new PythonBlock(pythonBody));
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
                AssignmentNode assignment = (AssignmentNode)expressionStatement.getExpression();
                return pythonAssignment(assignment.getTarget().accept(EXPRESSION_GENERATOR), generateExpression(assignment.getValue()));
            } else {
                return pythonExpressionStatement(generateExpression(expressionStatement.getExpression()));
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

    public static PythonExpressionNode generateExpression(ExpressionNode expression) {
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
        public PythonExpressionNode visit(ArrayNode array) {
            return pythonList(eagerMap(
                array.getElements(),
                PythonCodeGenerator::generateExpression));
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
                .orElseGet(() -> pythonCall(pythonAttributeAccess(receiver, Names.toUniqueName(methodCall.signature())), arguments));
        }

        private Optional<PythonExpressionNode> getPrimitiveMethod(
            MethodCallNode methodCall,
            PythonExpressionNode pythonReceiver,
            List<PythonExpressionNode> pythonArguments
        ) {
            return methodCall.getReceiver().accept(new Receiver.Mapper<Optional<PythonExpressionNode>>() {
                @Override
                public Optional<PythonExpressionNode> visit(ExpressionNode receiver) {
                    return PythonPrimitiveMethods.getPrimitiveMethod(erasure(receiver.getType()), methodCall.getMethodName())
                        .map(generator -> generator.generate(pythonReceiver, pythonArguments));
                }

                @Override
                public Optional<PythonExpressionNode> visit(ScalarType receiver) {
                    return PythonPrimitiveMethods.getPrimitiveStaticMethod(receiver, methodCall.getMethodName())
                        .map(generator -> generator.generate(pythonArguments));
                }
            });
        }

        @Override
        public PythonExpressionNode visit(ConstructorCallNode call) {
            final Type className = call.getType();
            final org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode classReference = typeReference(className);
            final java.util.List<org.zwobble.couscous.backends.python.ast.PythonExpressionNode> arguments = generateExpressions(call.getArguments());
            return pythonCall(classReference, arguments);
        }

        @Override
        public PythonExpressionNode visit(OperationNode operation) {
            return generateExpression(operation.desugar());
        }

        @Override
        public PythonExpressionNode visit(FieldAccessNode fieldAccess) {
            return pythonAttributeAccess(generateReceiver(fieldAccess.getLeft()), fieldAccess.getFieldName());
        }

        @Override
        public PythonExpressionNode visit(TypeCoercionNode typeCoercion) {
            PythonExpressionNode value = generateExpression(typeCoercion.getExpression());
            if (isIntegerBox(typeCoercion)) {
                // TODO: perform this replacement at the start so that we don't split the import handling and actual expression
                return visit(ConstructorCallNode.constructorCall(
                    Types.BOXED_INT,
                    list(typeCoercion.getExpression())));
            } else if (isIntegerUnbox(typeCoercion)) {
                return pythonAttributeAccess(value, "_value");
            } else {
                return value;
            }
        }

        @Override
        public PythonExpressionNode visit(CastNode cast) {
            // TODO: implement this properly
            // Can't test this properly (i.e. in one test, rather than one per backend)
            // until we have some unified notion of exceptions between backends
            return generateExpression(cast.getExpression());
        }
    }

    private static PythonExpressionNode generateReceiver(Receiver receiver) {
        return receiver.accept(new Receiver.Mapper<PythonExpressionNode>() {
            @Override
            public PythonExpressionNode visit(ExpressionNode receiver) {
                return generateExpression(receiver);
            }

            @Override
            public PythonExpressionNode visit(ScalarType receiver) {
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

    private static boolean isInteger(Type type) {
        return type.equals(Types.INT) || type.equals(Types.BOXED_INT);
    }

    private static PythonVariableReferenceNode typeReference(Type className) {
        return pythonVariableReference(erasure(className).getSimpleName());
    }
}