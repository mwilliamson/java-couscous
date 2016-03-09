package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.AnonymousClass;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.ast.sugar.TypeDeclarationBody;
import org.zwobble.couscous.ast.types.ScalarType;
import org.zwobble.couscous.ast.types.Type;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.util.ExtraLists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorNode.constructor;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.types.Types.erasure;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;
import static org.zwobble.couscous.frontends.java.JavaTypes.*;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.ExtraMaps.lookup;

public class JavaReader {
    // TODO: de-dupe captures (multiple reference nodes may reference the same free variable)
    public static List<TypeNode> readClassFromFile(List<Path> sourcePaths, Path sourcePath) throws IOException {
        CompilationUnit ast = new JavaParser().parseCompilationUnit(sourcePaths, sourcePath);

        ImmutableList<IProblem> errors = ImmutableList.copyOf(filter(asList(ast.getProblems()), problem -> problem.isError()));
        if (!errors.isEmpty()) {
            throw new RuntimeException("Errors during parsing:\n\n" + describe(errors));
        }
        JavaReader reader = new JavaReader();
        return reader.readCompilationUnit(ast);
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
    private final Map<ScalarType, List<ReferenceNode>> nestedClasses = new HashMap<>();

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private List<TypeNode> readCompilationUnit(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        List<TypeNode> classes = cons(readTypeDeclaration(type), this.classes.build());
        NodeTransformer transformer = NodeTransformer.builder()
            .transformExpression(expression ->
                tryCast(ConstructorCallNode.class, expression)
                    .flatMap(call -> lookup(nestedClasses, erasure(call.getType())).map(captures ->
                        new ConstructorCallNode(call.getType(), ExtraLists.concat(captures, call.getArguments())))))
            .build();
        return eagerMap(classes, classNode -> classNode.transform(transformer));
    }

    private TypeNode readTypeDeclaration(TypeDeclaration type) {
        ScalarType name = erasure(typeOf(type.resolveBinding()));
        Scope scope = topScope.enterClass(name);
        List<FormalTypeParameterNode> typeParameters = readTypeParameters(type);
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

    private List<FormalTypeParameterNode> readTypeParameters(TypeDeclaration typeDeclaration) {
        return eagerMap(
            asList(typeDeclaration.resolveBinding().getTypeParameters()),
            parameter -> formalTypeParameter(parameter.getName()));
    }

    GeneratedClosure readExpressionMethodReference(Scope outerScope, ExpressionMethodReference expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaExpressionMethodReferenceReader(this).toLambda(scope, expression));
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
        MethodNode method = MethodNode.method(
            emptyList(),
            false,
            functionalInterfaceMethod.getName(),
            lambda.getFormalArguments(),
            typeOf(functionalInterfaceMethod.getReturnType()),
            Optional.of(lambda.getBody()));

        return new AnonymousClass(
            superTypesAndSelf(functionalInterfaceMethod.getDeclaringClass()),
            list(),
            list(method));
    }

    private List<StatementNode> replaceCaptureReferences(
        ScalarType className,
        List<StatementNode> body,
        List<CapturedVariable> freeVariables)
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
        List<CapturedVariable> freeVariables,
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
        List<ReferenceNode> freeVariables = eagerFilter(
            findFreeVariables(ExtraLists.concat(
                classNode.getFields(),
                classNode.getMethods())),
            // TODO: check this references work correctly in anonymous classes
            variable -> !isThisReference(classNode.getName(), variable));
        List<CapturedVariable> capturedVariables = ImmutableList.copyOf(transform(
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
                .ifPresent(method -> readMethod(body, scope, method));

            tryCast(Initializer.class, declaration)
                .ifPresent(initializer -> body.addInitializer(
                    Modifier.isStatic(initializer.getModifiers()),
                    readStatement(scope, initializer.getBody())));

            tryCast(FieldDeclaration.class, declaration)
                .ifPresent(field -> readField(body, scope, type, field));

            tryCast(TypeDeclaration.class, declaration)
                .ifPresent(typeDeclaration -> classes.add(readNestedTypeDeclaration(type, typeDeclaration)));
        }
        return body.build();
    }

    private TypeNode readNestedTypeDeclaration(ScalarType outerType, TypeDeclaration typeDeclaration) {
        TypeNode typeNode = readTypeDeclaration(typeDeclaration);
        // TODO: can we remove duplication of scope creation with readTypeDeclaration()?
        Scope scope = topScope.enterClass(typeNode.getName());
        return tryCast(ClassNode.class, typeNode)
            .filter(node -> !Modifier.isStatic(typeDeclaration.getModifiers()))
            .<TypeNode>map(node -> {
                GeneratedClosure closure = classWithCapture(scope, node);
                nestedClasses.put(node.getName(), closure.getCaptures());
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

    private void readMethod(TypeDeclarationBody.Builder builder, Scope outerScope, MethodDeclaration method) {
        Scope scope = outerScope.enterMethod(method.getName().getIdentifier());

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
            builder.addMethod(MethodNode.method(
                annotations,
                Modifier.isStatic(method.getModifiers()),
                method.getName().getIdentifier(),
                formalArguments,
                returnType.get(),
                body));
        }
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

    ExpressionNode readExpression(Scope scope, org.zwobble.couscous.ast.types.Type targetType, Expression body) {
        return expressionReader(scope).readExpression(targetType, body);
    }

    ExpressionNode readExpressionWithoutBoxing(Scope scope, Expression body) {
        return expressionReader(scope).readExpressionWithoutBoxing(body);
    }

    private JavaExpressionReader expressionReader(Scope scope) {
        return new JavaExpressionReader(scope, this);
    }
}