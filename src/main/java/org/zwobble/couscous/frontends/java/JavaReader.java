package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.identifiers.Identifier;
import org.zwobble.couscous.ast.sugar.AnonymousClass;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.ast.sugar.TypeDeclarationBody;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraLists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorNode.constructor;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.InstanceReceiver.instanceReceiver;
import static org.zwobble.couscous.ast.StaticReceiver.staticReceiver;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.frontends.java.JavaMethods.signature;
import static org.zwobble.couscous.frontends.java.JavaTypes.*;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.only;
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
        TypeDeclarationBody body = readTypeDeclarationBody(scope, name, type.bodyDeclarations(), type.isInterface());
        Set<Type> superTypes = superTypes(type);
        if (type.isInterface()) {
            // TODO: throw exception if other parts of body are declared
            return InterfaceNode.declareInterface(
                name,
                typeParameters,
                superTypes,
                body.getFields(),
                body.getStaticConstructor(),
                body.getMethods(),
                body.getInnerTypes()
            );
        } else {
            return ClassNode.declareClass(
                name,
                typeParameters,
                superTypes,
                body.getFields(),
                body.getStaticConstructor(),
                body.getConstructor(),
                body.getMethods(),
                body.getInnerTypes()
            );
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

    AnonymousClass readExpressionMethodReference(Scope outerScope, ExpressionMethodReference expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaMethodReferenceReader(this).toLambda(scope, expression));
    }

    AnonymousClass readCreationReference(Scope outerScope, CreationReference expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaMethodReferenceReader(this).toLambda(scope, expression));
    }

    AnonymousClass readLambda(Scope outerScope, LambdaExpression expression) {
        return readLambda(
            outerScope,
            expression,
            scope -> new JavaLambdaExpressionReader(this).toLambda(scope, expression));
    }

    private AnonymousClass readLambda(Scope outerScope, Expression expression, Function<Scope, Lambda> lambda) {
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();
        return toAnonymousClass(functionalInterfaceMethod, lambda.apply(outerScope));
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
            Optional.empty(),
            only(superTypesAndSelf(functionalInterfaceMethod.getDeclaringClass())),
            list(),
            list(method));
    }

    AnonymousClass readAnonymousClass(Scope outerScope, AnonymousClassDeclaration declaration) {
        AnonymousType type = (AnonymousType) typeOf(declaration.resolveBinding());
        Scope scope = outerScope.enterType(type);
        TypeDeclarationBody bodyDeclarations = readTypeDeclarationBody(scope, type, declaration.bodyDeclarations(), false);
        return new AnonymousClass(
            Optional.of(type),
            only(superTypes(declaration)),
            bodyDeclarations.getFields(),
            bodyDeclarations.getMethods()
        );
    }

    private TypeDeclarationBody readTypeDeclarationBody(Scope scope, Type type, List<Object> bodyDeclarations, boolean isInterface) {
        TypeDeclarationBody.Builder body = TypeDeclarationBody.builder();

        for (Object declaration : bodyDeclarations) {
            tryCast(MethodDeclaration.class, declaration)
                .ifPresent(method -> readMethod(body, scope, method));

            tryCast(Initializer.class, declaration)
                .ifPresent(initializer -> body.addInitializer(
                    Modifier.isStatic(initializer.getModifiers()),
                    readStatementBody(scope, initializer.getBody())));

            tryCast(FieldDeclaration.class, declaration)
                .ifPresent(field -> readField(body, scope, type, field, isInterface));

            tryCast(TypeDeclaration.class, declaration)
                .ifPresent(typeDeclaration -> body.addInnerType(readTypeDeclaration(typeDeclaration)));
        }
        return body.build();
    }

    private void readField(TypeDeclarationBody.Builder builder, Scope scope, Type declaringType, FieldDeclaration field, boolean isInterface) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = field.fragments();
        boolean isStatic = isInterface || Modifier.isStatic(field.getModifiers());
        Type type = typeOf(field.getType());
        fragments.forEach(fragment -> {
            builder.field(field(isStatic, fragment.getName().getIdentifier(), type));
            if (fragment.getInitializer() != null) {
                ExpressionNode value = readExpression(scope, type, fragment.getInitializer());
                String name = fragment.getName().getIdentifier();
                // TODO: support static values for anonymous classes
                Receiver receiver = isStatic
                    ? staticReceiver((ScalarType)declaringType)
                    : instanceReceiver(thisReference(declaringType));

                StatementNode assignment = assignStatement(fieldAccess(receiver, name, type), value);
                builder.addInitializer(isStatic, assignment);
            }
        });

    }

    private void readMethod(TypeDeclarationBody.Builder builder, Scope outerScope, MethodDeclaration method) {
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
        return eagerMap(parameters, parameter -> formalArg(JavaVariableDeclarationReader.read(scope, parameter)));
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

    List<StatementNode> readStatementBody(Scope scope, Statement statement) {
        JavaStatementReader statementReader = new JavaStatementReader(scope, expressionReader(scope), Optional.empty());
        return statementReader.readBody(statement);
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
