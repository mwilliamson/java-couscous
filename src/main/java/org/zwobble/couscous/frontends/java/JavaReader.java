package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.sugar.AnonymousClass;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.ast.sugar.TypeDeclarationBody;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraLists;
import org.zwobble.couscous.util.InsertionOrderSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorCallNode.constructorCall;
import static org.zwobble.couscous.ast.ConstructorNode.constructor;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;
import static org.zwobble.couscous.frontends.java.JavaMethods.signature;
import static org.zwobble.couscous.frontends.java.JavaTypes.*;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.*;

public class JavaReader {
    public static List<TypeNode> readClassesFromFiles(List<Path> sourcePaths, List<Path> sourceFiles) throws IOException {
        JavaParser parser = new JavaParser();
        JavaReader reader = new JavaReader();
        for (Path sourceFile : sourceFiles) {
            CompilationUnit ast = parser.parseCompilationUnit(sourcePaths, sourceFile);

            List<IProblem> errors = eagerFilter(asList(ast.getProblems()), problem -> problem.isError());
            if (!errors.isEmpty()) {
                throw new RuntimeException("Errors during parsing:\n\n" + describe(errors));
            }
            try {
                reader.readCompilationUnit(ast);
            } catch (Exception exception) {
                throw new RuntimeException("Error reading " + sourceFile, exception);
            }
        }
        return reader.types();
    }

    private static String describe(List<IProblem> errors) {
        return String.join("\n\n", transform(errors, JavaReader::describe));
    }

    private static String describe(IProblem error) {
        return "File: " + String.copyValueOf(error.getOriginatingFileName()) + "\n" +
            "Line number: " + error.getSourceLineNumber() + "\n" +
            error.getMessage();
    }

    private final ImmutableList.Builder<TypeNode> classes;
    private int anonymousClassCount = 0;
    private final Scope topScope = Scope.create();

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private void readCompilationUnit(CompilationUnit ast) {
        AbstractTypeDeclaration type = (AbstractTypeDeclaration) ast.types().get(0);
        if (type instanceof TypeDeclaration) {
            classes.add(readTypeDeclaration((TypeDeclaration) type));
        } else if (type instanceof EnumDeclaration) {
            classes.add(readEnumDeclaration((EnumDeclaration)type));
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private List<TypeNode> types() {
        return this.classes.build();
    }

    private TypeNode readEnumDeclaration(EnumDeclaration type) {
        ScalarType name = (ScalarType) typeOf(type.resolveBinding());
        @SuppressWarnings("unchecked")
        List<EnumConstantDeclaration> values = type.enumConstants();
        return new EnumNode(name, eagerMap(values, declaration -> declaration.getName().getIdentifier()));
    }

    private TypeNode readTypeDeclaration(TypeDeclaration type) {
        ScalarType name = erasure(typeOf(type.resolveBinding()));
        Scope scope = topScope.enterClass(name);
        List<FormalTypeParameterNode> typeParameters = readTypeParameters(scope.getIdentifier(), type);
        TypeDeclarationBody body = readTypeDeclarationBody(scope, name, type.bodyDeclarations());
        Set<Type> superTypes = superTypes(type);
        if (type.isInterface()) {
            // TODO: throw exception if other parts of body are declared
            return InterfaceNode.declareInterface(name, typeParameters, superTypes, body.getMethods());
        } else {
            return ClassNode.declareClass(
                name,
                typeParameters,
                superTypes,
                body.getFields(),
                body.getStaticConstructor(),
                body.getConstructor(),
                body.getMethods());
        }
    }

    private List<FormalTypeParameterNode> readTypeParameters(Identifier declaringScope, TypeDeclaration typeDeclaration) {
        return readTypeParameters(declaringScope, typeDeclaration.resolveBinding().getTypeParameters());
    }

    private List<FormalTypeParameterNode> readTypeParameters(Identifier declaringScope, ITypeBinding[] typeParameters) {
        return eagerMap(
            asList(typeParameters),
            parameter -> formalTypeParameter(declaringScope, parameter.getName()));
    }

    GeneratedClosure readExpressionMethodReference(Scope outerScope, ExpressionMethodReference expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaMethodReferenceReader(this).toLambda(scope, expression));
    }

    GeneratedClosure readCreationReference(Scope outerScope, CreationReference expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaMethodReferenceReader(this).toLambda(scope, expression));
    }

    GeneratedClosure readLambda(Scope outerScope, LambdaExpression expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaLambdaExpressionReader(this).toLambda(scope, expression));
    }

    private GeneratedClosure readLambda(Scope outerScope, Expression expression, Function<Scope, Lambda> lambda) {
        ScalarType name = generateAnonymousName(expression);
        Scope scope = outerScope.enterClass(name);
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();
        return generateClosure(scope, name, toAnonymousClass(functionalInterfaceMethod, lambda.apply(scope)));
    }

    private AnonymousClass toAnonymousClass(IMethodBinding functionalInterfaceMethod, Lambda lambda) {
        // TODO: does this need type coercion adding? Since the body is straight from the lambda, but the type signature is from the interface method
        MethodNode method = MethodNode.method(
            emptyList(),
            false,
            functionalInterfaceMethod.getName(),
            // TODO: should get formal type parameters from the lambda
            list(),
            lambda.getFormalArguments(),
            typeOf(functionalInterfaceMethod.getReturnType()),
            Optional.of(lambda.getBody()),
            overrides(functionalInterfaceMethod));

        return new AnonymousClass(
            superTypesAndSelf(functionalInterfaceMethod.getDeclaringClass()),
            list(),
            list(method));
    }

    private List<StatementNode> replaceCaptureReferences(
        ScalarType className,
        List<StatementNode> body,
        InsertionOrderSet<CapturedVariable> freeVariables)
    {
        Map<ExpressionNode, ExpressionNode> replacements = Maps.transformValues(
            Maps.uniqueIndex(freeVariables, variable -> variable.freeVariable),
            freeVariable -> captureAccess(className, freeVariable));
        NodeTransformer transformer = NodeTransformer.replaceExpressions(replacements);
        return eagerMap(body, statement -> statement.transform(transformer));
    }

    private ConstructorNode buildConstructor(
        Scope outerScope,
        ScalarType type,
        InsertionOrderSet<CapturedVariable> freeVariables,
        ConstructorNode existing)
    {
        Scope scope = outerScope.enterConstructor();

        ImmutableList.Builder<FormalArgumentNode> arguments = ImmutableList.builder();
        ImmutableList.Builder<StatementNode> body = ImmutableList.builder();

        for (CapturedVariable variable : freeVariables) {
            FormalArgumentNode argument = scope.formalArgument(variable.field.getName(), variable.field.getType());
            arguments.add(argument);
            body.add(assignStatement(captureAccess(type, variable), reference(argument)));
        }

        arguments.addAll(existing.getArguments());
        body.addAll(existing.getBody());

        return new ConstructorNode(arguments.build(), body.build());
    }

    private FieldAccessNode captureAccess(ScalarType type, CapturedVariable freeVariable) {
        FieldDeclarationNode field = freeVariable.field;
        return fieldAccess(thisReference(type), field.getName(), field.getType());
    }

    GeneratedClosure readAnonymousClass(Scope outerScope, AnonymousClassDeclaration declaration) {
        ScalarType className = generateAnonymousName(declaration);
        Scope scope = outerScope.enterClass(className);
        TypeDeclarationBody bodyDeclarations = readTypeDeclarationBody(scope, className, declaration.bodyDeclarations());
        AnonymousClass anonymousClass = new AnonymousClass(
            superTypes(declaration),
            bodyDeclarations.getFields(),
            bodyDeclarations.getMethods());
        return generateClosure(scope, className, anonymousClass);
    }

    GeneratedClosure generateClosure(Scope scope, ScalarType className, AnonymousClass anonymousClass) {
        GeneratedClosure closure = classWithCapture(scope, className, anonymousClass);
        classes.add(closure.getClassNode());
        return closure;
    }

    private static class CapturedVariable {
        private final ReferenceNode freeVariable;
        private final FieldDeclarationNode field;

        public CapturedVariable(ReferenceNode freeVariable, FieldDeclarationNode field) {
            this.freeVariable = freeVariable;
            this.field = field;
        }
    }

    private GeneratedClosure classWithCapture(
        Scope scope,
        ClassNode classNode
    ) {
        InsertionOrderSet<ReferenceNode> freeVariables = InsertionOrderSet.copyOf(Iterables.filter(
            findFreeVariables(ExtraLists.concat(
                classNode.getFields(),
                classNode.getMethods())),
            // TODO: check this references work correctly in anonymous classes
            variable -> !isThisReference(classNode.getName(), variable)));
        InsertionOrderSet<CapturedVariable> capturedVariables = InsertionOrderSet.copyOf(transform(
            freeVariables,
            freeVariable -> new CapturedVariable(freeVariable, fieldForCapture(freeVariable))));
        Iterable<FieldDeclarationNode> captureFields = transform(capturedVariables, capture -> capture.field);

        List<FieldDeclarationNode> fields = ImmutableList.copyOf(concat(
            classNode.getFields(),
            captureFields));

        if (!classNode.getStaticConstructor().isEmpty()) {
            throw new RuntimeException("Class has unexpected static constructor");
        }

        ClassNode generatedClass = new ClassNode(
            classNode.getName(),
            classNode.getTypeParameters(),
            classNode.getSuperTypes(),
            fields,
            list(),
            buildConstructor(scope, classNode.getName(), capturedVariables, classNode.getConstructor()),
            eagerMap(classNode.getMethods(), method ->
                method.mapBody(body -> replaceCaptureReferences(classNode.getName(), body, capturedVariables))));
        return new GeneratedClosure(generatedClass, freeVariables);
    }

    private boolean isThisReference(ScalarType name, ReferenceNode reference) {
        return tryCast(ThisReferenceNode.class, reference)
            .map(node -> node.getType().equals(name))
            .orElse(false);
    }

    private GeneratedClosure classWithCapture(
        Scope scope,
        ScalarType className,
        AnonymousClass anonymousClass
    ) {
        return classWithCapture(scope, ClassNode.declareClass(
            className,
            list(),
            anonymousClass.getSuperTypes(),
            anonymousClass.getFields(),
            list(),
            ConstructorNode.DEFAULT,
            anonymousClass.getMethods()));
    }

    private FieldDeclarationNode fieldForCapture(ReferenceNode freeVariable) {
        return freeVariable.accept(new ReferenceNode.Visitor<FieldDeclarationNode>() {
            @Override
            public FieldDeclarationNode visit(VariableReferenceNode reference) {
                return field(reference.getReferent().getName(), reference.getType());
            }

            @Override
            public FieldDeclarationNode visit(ThisReferenceNode thisReference) {
                Type type = thisReference.getType();
                String name = "this_" + erasure(type).getQualifiedName().replace(".", "__");
                return field(name, type);
            }
        });
    }

    private ScalarType generateAnonymousName(ASTNode node) {
        ITypeBinding type = findDeclaringClass(node);
        while (type.isAnonymous()) {
            type = type.getDeclaringClass();
        }
        return ScalarType.of(type.getQualifiedName() + "_Anonymous_" + (anonymousClassCount++));
    }

    private ITypeBinding findDeclaringClass(ASTNode node) {
        while (!(node instanceof AbstractTypeDeclaration)) {
            node = node.getParent();
        }
        return ((AbstractTypeDeclaration)node).resolveBinding();
    }

    private TypeDeclarationBody readTypeDeclarationBody(Scope scope, ScalarType type, List<Object> bodyDeclarations) {
        TypeDeclarationBody.Builder body = TypeDeclarationBody.builder();

        for (Object declaration : bodyDeclarations) {
            tryCast(MethodDeclaration.class, declaration)
                .ifPresent(method -> readMethod(body, scope, type, method));

            tryCast(Initializer.class, declaration)
                .ifPresent(initializer -> body.addInitializer(
                    Modifier.isStatic(initializer.getModifiers()),
                    readStatement(scope, initializer.getBody())));

            tryCast(FieldDeclaration.class, declaration)
                .ifPresent(field -> readField(body, scope, type, field));

            tryCast(TypeDeclaration.class, declaration)
                .ifPresent(typeDeclaration -> classes.add(readNestedTypeDeclaration(body, typeDeclaration)));
        }
        return body.build();
    }

    private TypeNode readNestedTypeDeclaration(TypeDeclarationBody.Builder body, TypeDeclaration typeDeclaration) {
        TypeNode typeNode = readTypeDeclaration(typeDeclaration);
        // TODO: can we remove duplication of scope creation with readTypeDeclaration()?
        Scope scope = topScope.enterClass(typeNode.getName());
        return tryCast(ClassNode.class, typeNode)
            .filter(node -> !Modifier.isStatic(typeDeclaration.getModifiers()))
            .<TypeNode>map(node -> {
                GeneratedClosure closure = classWithCapture(scope, node);
//                Type type = node.getTypeParameters().isEmpty()
//                    ? typeNode.getName()
//                    : parameterizedType(typeNode.getName(), eagerMap(node.getTypeParameters()));
                Type type = typeNode.getName();
                // TODO: method type parameters
                List<FormalArgumentNode> methodArguments = eagerMap(
                    node.getConstructor().getArguments(),
                    // TODO: use scope of outer class
                    argument -> scope.formalArgument(argument.getName(), argument.getType()));
                List<ExpressionNode> constructorArguments = ExtraLists.concat(
                    closure.getCaptures(),
                    lazyMap(methodArguments, argument -> reference(argument)));
                MethodNode method = MethodNode.builder("create_" + typeDeclaration.getName().getIdentifier())
                    .arguments(methodArguments)
                    .returns(type)
                    .statement(returns(constructorCall(type, constructorArguments)))
                    .build();
                body.addMethod(method);
                return closure.getClassNode();
            })
            .orElse(typeNode);
    }

    private void readField(TypeDeclarationBody.Builder builder, Scope scope, ScalarType declaringType, FieldDeclaration field) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = field.fragments();
        Type type = typeOf(field.getType());
        fragments.forEach(fragment -> {
            builder.field(field(Modifier.isStatic(field.getModifiers()), fragment.getName().getIdentifier(), type));
            if (fragment.getInitializer() != null) {
                ExpressionNode value = readExpression(scope, type, fragment.getInitializer());
                String name = fragment.getName().getIdentifier();
                boolean isStatic = Modifier.isStatic(field.getModifiers());
                Receiver receiver = isStatic
                    ? staticReceiver(declaringType)
                    : instanceReceiver(thisReference(declaringType));

                StatementNode assignment = assignStatement(fieldAccess(receiver, name, type), value);
                builder.addInitializer(isStatic,assignment);
            }
        });

    }

    private void readMethod(TypeDeclarationBody.Builder builder, Scope outerScope, ScalarType type, MethodDeclaration method) {
        String methodName = method.getName().getIdentifier();
        Scope scope = outerScope.enterMethod(methodName);

        List<FormalArgumentNode> formalArguments = readFormalArguments(scope, method);
        List<AnnotationNode> annotations = readAnnotations(method);
        Optional<Type> returnType = Optional.ofNullable(method.getReturnType2())
            .map(JavaTypes::typeOf);
        Optional<List<StatementNode>> body = readBody(scope, method, returnType);

        if (method.isConstructor()) {
            builder.constructor(constructor(
                formalArguments,
                body.get()));
        } else {
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            MethodNode methodNode = MethodNode.method(
                annotations,
                isStatic,
                methodName,
                readTypeParameters(scope.getIdentifier(), method.resolveBinding().getTypeParameters()),
                formalArguments,
                returnType.get(),
                body,
                overrides(method.resolveBinding()));
            builder.addMethod(methodNode);
        }
    }

    private List<MethodSignature> overrides(IMethodBinding methodBinding) {
        MethodSignature overrideSignature = signature(methodBinding);
        ITypeBinding declaringClass = methodBinding.getDeclaringClass();

        // TODO: remove duplication with JavaTypes
        // TODO: consider superclass
        return ExtraLists.concat(list(declaringClass), asList(declaringClass.getInterfaces()))
            .stream()
            .flatMap(type -> asList(type.getDeclaredMethods()).stream().filter(superMethod -> methodBinding.overrides(superMethod)))
            .map(JavaMethods::signature)
            .filter(signature -> !overrideSignature.equals(signature))
            .collect(Collectors.toList());
    }

    private List<AnnotationNode> readAnnotations(MethodDeclaration method) {
        return eagerMap(asList(method.resolveBinding().getAnnotations()), this::readAnnotation);
    }

    private AnnotationNode readAnnotation(IAnnotationBinding annotationBinding) {
        return annotation(typeOf(annotationBinding.getAnnotationType()));
    }

    private List<FormalArgumentNode> readFormalArguments(Scope scope, MethodDeclaration method) {
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = method.parameters();
        return eagerMap(parameters, parameter -> JavaVariableDeclarationReader.read(scope, parameter));
    }

    private Optional<List<StatementNode>> readBody(Scope scope, MethodDeclaration method, Optional<Type> returnType) {
        if (method.getBody() == null) {
            return Optional.empty();
        } else {
            @SuppressWarnings("unchecked")
            List<Statement> statements = method.getBody().statements();
            return Optional.of(readStatements(scope, statements, returnType));
        }
    }

    List<StatementNode> readStatement(Scope scope, Statement statement) {
        JavaStatementReader statementReader = new JavaStatementReader(scope, expressionReader(scope), Optional.empty());
        return statementReader.readStatement(statement);
    }

    List<StatementNode> readStatements(Scope scope, List<Statement> body, Optional<Type> returnType) {
        JavaStatementReader statementReader = new JavaStatementReader(scope, expressionReader(scope), returnType);
        return eagerFlatMap(body, statementReader::readStatement);
    }

    ExpressionNode readExpression(Scope scope, Type targetType, Expression body) {
        return expressionReader(scope).readExpression(targetType, body);
    }

    ExpressionNode readExpressionWithoutBoxing(Scope scope, Expression body) {
        return expressionReader(scope).readExpressionWithoutBoxing(body);
    }

    private JavaExpressionReader expressionReader(Scope scope) {
        return new JavaExpressionReader(scope, this);
    }
}