package org.zwobble.couscous.backends.python;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.structure.NodeStructure;
import org.zwobble.couscous.ast.visitors.DynamicNodeMapper;
import org.zwobble.couscous.backends.naming.Names;
import org.zwobble.couscous.backends.python.ast.*;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.values.InternalCouscousValue;
import org.zwobble.couscous.values.PrimitiveValue;
import org.zwobble.couscous.values.TypeValue;

import java.util.*;
import java.util.function.Function;
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
            .flatMap(FindDirectlyReferencedClasses.VISITOR)
            .collect(Collectors.toSet());
    }

    public static class FindDirectlyReferencedClasses {
        private static final Function<Node, Stream<ScalarType>> VISITOR =
            DynamicNodeMapper.instantiate(new FindDirectlyReferencedClasses(), "visit");

        public Stream<ScalarType> visit(Node node) {
            return Stream.empty();
        }

        public Stream<ScalarType> visit(StaticReceiver receiver) {
            return Stream.of(receiver.getType());
        }

        public Stream<ScalarType> visit(ConstructorCallNode call) {
            return Stream.of(erasure(call.getType()));
        }

        public Stream<ScalarType> visit(TypeCoercionNode typeCoercion) {
            if (isIntegerBox(typeCoercion)) {
                return Stream.of(Types.BOXED_INT);
            } else {
                return Stream.empty();
            }
        }

        public Stream<ScalarType> visit(LiteralNode literal) {
            if (literal.getValue() instanceof TypeValue) {
                return Stream.of(((TypeValue)literal.getValue()).getValue());
            } else {
                return Stream.empty();
            }
        }
    }

    public static PythonExpressionNode generateCode(PrimitiveValue value) {
        return value.accept(new PrimitiveValue.Visitor<PythonExpressionNode>(){

            @Override
            public PythonExpressionNode visitInteger(int value) {
                return pythonIntegerLiteral(value);
            }

            @Override
            public PythonExpressionNode visitChar(char value) {
                // TODO:
                throw new UnsupportedOperationException();
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
        List<PythonStatementNode> pythonBody = generateStatements(constructor.getBody());
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
                    methodNode.getReturnType(),
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
        return eagerFlatMap(statements, PythonCodeGenerator::generateStatement);
    }

    private static List<PythonStatementNode> generateStatement(StatementNode statement) {
        return StatementGenerator.VISITOR.apply(statement);
    }

    public static class StatementGenerator {
        private static final Function<Node, List<PythonStatementNode>> VISITOR = DynamicNodeMapper.instantiate(new StatementGenerator(), "visit");

        public List<PythonStatementNode> visit(ReturnNode returnNode) {
            return list(pythonReturn(generateExpression(returnNode.getValue())));
        }

        public List<PythonStatementNode> visit(ExpressionStatementNode expressionStatement) {
            if (expressionStatement.getExpression() instanceof AssignmentNode) {
                AssignmentNode assignment = (AssignmentNode)expressionStatement.getExpression();
                return list(pythonAssignment(generateExpression(assignment.getTarget()), generateExpression(assignment.getValue())));
            } else {
                return list(pythonExpressionStatement(generateExpression(expressionStatement.getExpression())));
            }
        }

        public List<PythonStatementNode> visit(LocalVariableDeclarationNode declaration) {
            return list(pythonAssignment(pythonVariableReference(declaration.getDeclaration().getName()), generateExpression(declaration.getInitialValue())));
        }

        public List<PythonStatementNode> visit(IfStatementNode ifStatement) {
            return list(pythonIfStatement(
                generateExpression(ifStatement.getCondition()),
                generateStatements(ifStatement.getTrueBranch()),
                generateStatements(ifStatement.getFalseBranch())
            ));
        }

        public List<PythonStatementNode> visit(WhileNode whileLoop) {
            return list(pythonWhile(
                generateExpression(whileLoop.getCondition()),
                generateStatements(whileLoop.getBody())
            ));
        }

        public List<PythonStatementNode> visit(StatementBlockNode block) {
            // TODO: this doesn't handle separate variables in different blocks with the same name properly
            return generateStatements(block.getStatements());
        }
    }

    public static PythonExpressionNode generateExpression(ExpressionNode expression) {
        return ExpressionGenerator.VISITOR.apply(expression);
    }

    private static List<PythonExpressionNode> generateExpressions(Iterable<? extends ExpressionNode> expressions) {
        return ImmutableList.copyOf(transform(expressions, PythonCodeGenerator::generateExpression));
    }

    public static class ExpressionGenerator {
        private static final Function<Node, PythonExpressionNode> VISITOR =
            DynamicNodeMapper.instantiate(new ExpressionGenerator(), "visit");

        public PythonExpressionNode visit(LiteralNode literal) {
            return generateCode(literal.getValue());
        }

        public PythonVariableReferenceNode visit(VariableReferenceNode variableReference) {
            return pythonVariableReference(variableReference.getReferent().getName());
        }

        public PythonExpressionNode visit(ThisReferenceNode reference) {
            return pythonVariableReference("self");
        }

        public PythonExpressionNode visit(ArrayNode array) {
            return pythonList(eagerMap(
                array.getElements(),
                PythonCodeGenerator::generateExpression));
        }

        public PythonExpressionNode visit(TernaryConditionalNode ternaryConditional) {
            return pythonConditionalExpression(generateExpression(ternaryConditional.getCondition()), generateExpression(ternaryConditional.getIfTrue()), generateExpression(ternaryConditional.getIfFalse()));
        }

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

        public PythonExpressionNode visit(ConstructorCallNode call) {
            final Type className = call.getType();
            final org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode classReference = typeReference(className);
            final java.util.List<org.zwobble.couscous.backends.python.ast.PythonExpressionNode> arguments = generateExpressions(call.getArguments());
            return pythonCall(classReference, arguments);
        }

        public PythonExpressionNode visit(OperationNode operation) {
            return generateExpression(operation.desugar());
        }

        public PythonExpressionNode visit(FieldAccessNode fieldAccess) {
            return pythonAttributeAccess(generateReceiver(fieldAccess.getLeft()), fieldAccess.getFieldName());
        }

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
