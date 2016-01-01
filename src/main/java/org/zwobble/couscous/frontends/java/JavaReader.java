package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.VariableDeclaration;
import org.zwobble.couscous.ast.sugar.Lambda;
import org.zwobble.couscous.util.ExtraLists;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.zwobble.couscous.ast.AnnotationNode.annotation;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.ConstructorNode.constructor;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FormalArgumentNode.formalArg;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableDeclaration.var;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.sugar.Lambda.lambda;
import static org.zwobble.couscous.frontends.java.JavaExpressionMethodReferenceReader.javaExpressionmethodReferenceToLambda;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;
import static org.zwobble.couscous.frontends.java.JavaTypes.*;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraLists.*;

public class JavaReader {
    public static List<ClassNode> readClassFromFile(Path root, Path sourcePath) throws IOException {
        CompilationUnit ast = new JavaParser().parseCompilationUnit(root, sourcePath);
        System.out.println(sourcePath);
        for (Message message : ast.getMessages()) {
            System.out.println(message.getMessage());
        }
        JavaReader reader = new JavaReader();
        return cons(reader.readCompilationUnit(ast), reader.classes.build());
    }

    private final UniqueIdentifiers identifiers = new UniqueIdentifiers();
    private final ImmutableList.Builder<ClassNode> classes;
    private int anonymousClassCount = 0;

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private ClassNode readCompilationUnit(CompilationUnit ast) {
        TypeName name = generateClassName(ast);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        TypeDeclarationBody body = readTypeDeclarationBody(type.bodyDeclarations());
        return ClassNode.declareClass(
            name,
            superTypes(type),
            body.getFields(),
            body.getConstructor(),
            body.getMethods());
    }

    GeneratedClosure readExpressionMethodReference(ExpressionMethodReference expression) {
        return generateClosure(
            expression,
            expression.resolveTypeBinding().getFunctionalInterfaceMethod(),
            javaExpressionmethodReferenceToLambda(expression));
    }

    private ITypeBinding findDeclaringClass(ASTNode node) {
        while (!(node instanceof AbstractTypeDeclaration)) {
            node = node.getParent();
        }
        return ((AbstractTypeDeclaration)node).resolveBinding();
    }

    GeneratedClosure readLambda(LambdaExpression expression) {
        return generateClosure(
            expression,
            expression.resolveTypeBinding().getFunctionalInterfaceMethod(),
            javaLambdaToLambda(expression));
    }

    GeneratedClosure generateClosure(ASTNode node, IMethodBinding functionalInterfaceMethod, Lambda lambda) {
        MethodNode method = MethodNode.method(
            emptyList(),
            false,
            functionalInterfaceMethod.getName(),
            lambda.getFormalArguments(),
            lambda.getBody());

        GeneratedClosure closure = classWithCapture(
            generateAnonymousClassName(findDeclaringClass(node)),
            superTypesAndSelf(functionalInterfaceMethod.getDeclaringClass()),
            emptyList(),
            ImmutableList.of(method));

        classes.add(closure.getClassNode());
        return closure;
    }

    private List<StatementNode> replaceCaptureReferences(
        TypeName className,
        List<StatementNode> body,
        List<VariableDeclaration> freeVariables)
    {
        Map<String, ExpressionNode> freeVariablesById = Maps.transformValues(
            Maps.uniqueIndex(freeVariables, VariableDeclaration::getId),
            freeVariable -> captureAccess(className, freeVariable));
        Function<ExpressionNode, ExpressionNode> replaceExpression = new CaptureReplacer(freeVariablesById);
        return eagerMap(body, statement -> statement.replaceExpressions(replaceExpression));
    }

    private class CaptureReplacer implements Function<ExpressionNode, ExpressionNode> {
        private final Map<String, ExpressionNode> freeVariablesById;

        public CaptureReplacer(Map<String, ExpressionNode> freeVariablesById) {
            this.freeVariablesById = freeVariablesById;
        }

        @Override
        public ExpressionNode apply(ExpressionNode expression) {
            return tryCast(VariableReferenceNode.class, expression)
                .flatMap(variableNode -> Optional.ofNullable(freeVariablesById.get(variableNode.getReferentId())))
                .orElseGet(() -> expression.replaceExpressions(this));
        }
    }

    private ConstructorNode buildConstructor(TypeName type, List<VariableDeclaration> freeVariables) {
        Map<String, VariableDeclaration> argumentDeclarationsById = Maps.transformValues(
            Maps.uniqueIndex(freeVariables, VariableDeclaration::getId),
            freeVariable -> var(identifiers.generate(freeVariable.getId() + "__capture"), freeVariable.getName(), freeVariable.getType()));
        List<FormalArgumentNode> arguments = eagerMap(
            freeVariables,
            freeVariable -> formalArg(argumentDeclarationsById.get(freeVariable.getId())));
        List<StatementNode> body = eagerMap(freeVariables, freeVariable -> assignStatement(
            captureAccess(type, freeVariable),
            reference(argumentDeclarationsById.get(freeVariable.getId()))));
        return constructor(arguments, body);
    }

    private FieldAccessNode captureAccess(TypeName type, VariableDeclaration freeVariable) {
        return fieldAccess(thisReference(type), freeVariable.getName(), freeVariable.getType());
    }

    GeneratedClosure readAnonymousClass(AnonymousClassDeclaration declaration) {
        TypeName className = generateClassName(declaration);
        TypeDeclarationBody bodyDeclarations = readTypeDeclarationBody(declaration.bodyDeclarations());
        GeneratedClosure closure = classWithCapture(
            className,
            superTypes(declaration),
            bodyDeclarations.getFields(),
            bodyDeclarations.getMethods());
        classes.add(closure.getClassNode());
        return closure;
    }

    private GeneratedClosure classWithCapture(
        TypeName className,
        Set<TypeName> superTypes,
        List<FieldDeclarationNode> declaredFields,
        List<MethodNode> methods
    ) {
        List<VariableDeclaration> freeVariables = findFreeVariables(ExtraLists.concat(declaredFields, methods));
        Iterable<FieldDeclarationNode> captureFields = transform(
            freeVariables,
            freeVariable -> field(freeVariable.getName(), freeVariable.getType()));

        List<FieldDeclarationNode> fields = ImmutableList.copyOf(concat(declaredFields, captureFields));

        ClassNode classNode = ClassNode.declareClass(
            className,
            superTypes,
            fields,
            buildConstructor(className, freeVariables),
            eagerMap(methods, method ->
                method.mapBody(body -> replaceCaptureReferences(className, body, freeVariables))));
        return new GeneratedClosure(classNode, freeVariables);
    }

    private TypeName generateClassName(AnonymousClassDeclaration declaration) {
        ITypeBinding type = declaration.resolveBinding();
        return generateAnonymousClassName(type);
    }

    private TypeName generateAnonymousClassName(ITypeBinding type) {
        while (type.isAnonymous()) {
            type = type.getDeclaringClass();
        }
        return TypeName.of(type.getQualifiedName() + "_Anonymous_" + (anonymousClassCount++));
    }

    private static class TypeDeclarationBody {
        private final List<FieldDeclarationNode> fields;
        private final ConstructorNode constructor;
        private final List<MethodNode> methods;

        public TypeDeclarationBody(List<FieldDeclarationNode> fields, ConstructorNode constructor, List<MethodNode> methods) {
            this.fields = fields;
            this.constructor = constructor;
            this.methods = methods;
        }

        public List<FieldDeclarationNode> getFields() {
            return fields;
        }

        public ConstructorNode getConstructor() {
            return constructor;
        }

        public List<MethodNode> getMethods() {
            return methods;
        }
    }

    private TypeDeclarationBody readTypeDeclarationBody(List<Object> bodyDeclarations) {
        ImmutableList.Builder<MethodNode> methods = ImmutableList.builder();
        ConstructorNode constructor = ConstructorNode.DEFAULT;

        for (CallableNode callable : readMethods(ofType(bodyDeclarations, MethodDeclaration.class))) {
            if (callable instanceof ConstructorNode) {
                constructor = (ConstructorNode) callable;
            } else {
                methods.add((MethodNode) callable);
            }
        }
        return new TypeDeclarationBody(
            readFields(ofType(bodyDeclarations, FieldDeclaration.class)),
            constructor,
            methods.build());
    }

    private List<FieldDeclarationNode> readFields(List<FieldDeclaration> fields) {
        return eagerFlatMap(fields, this::readField);
    }

    private List<FieldDeclarationNode> readField(FieldDeclaration field) {
        @SuppressWarnings("unchecked")
        List<VariableDeclarationFragment> fragments = field.fragments();
        TypeName type = typeOf(field.getType());
        return eagerMap(fragments, fragment ->
            field(fragment.getName().getIdentifier(), type));
    }

    private List<CallableNode> readMethods(List<MethodDeclaration> methods) {
        return eagerMap(methods, this::readMethod);
    }

    private CallableNode readMethod(MethodDeclaration method) {
        FunctionDeclaration function = functionDeclaration(method);
        if (method.isConstructor()) {
            return constructor(
                function.getFormalArguments(),
                function.getBody());
        } else {
            return MethodNode.method(
                function.getAnnotations(),
                Modifier.isStatic(method.getModifiers()),
                method.getName().getIdentifier(),
                function.getFormalArguments(),
                function.getBody());
        }
    }

    private FunctionDeclaration functionDeclaration(MethodDeclaration method) {
        @SuppressWarnings("unchecked")
        List<SingleVariableDeclaration> parameters = method.parameters();
        List<FormalArgumentNode> formalArguments = eagerMap(parameters, this::readSingleVariableDeclaration);
        @SuppressWarnings("unchecked")
        List<Statement> statements = method.getBody().statements();
        Optional<TypeName> returnType = Optional.ofNullable(method.getReturnType2())
            .map(Type::resolveBinding)
            .map(JavaTypes::typeOf);

        return new FunctionDeclaration(
            eagerMap(asList(method.resolveBinding().getAnnotations()), this::readAnnotation),
            formalArguments,
            readStatements(statements, returnType));
    }

    private Lambda javaLambdaToLambda(LambdaExpression expression) {
        List<FormalArgumentNode> formalArguments = eagerMap(
            (List<?>)expression.parameters(),
            this::readLambdaExpressionParameter);

        return lambda(formalArguments, readLambdaExpressionBody(expression));
    }

    private AnnotationNode readAnnotation(IAnnotationBinding annotationBinding) {
        return annotation(typeOf(annotationBinding.getAnnotationType()));
    }

    private FormalArgumentNode readLambdaExpressionParameter(Object parameter) {
        if (parameter instanceof SingleVariableDeclaration) {
            return readSingleVariableDeclaration((SingleVariableDeclaration) parameter);
        } else {
            VariableDeclarationFragment fragment = (VariableDeclarationFragment)parameter;
            return formalArg(var(
                fragment.resolveBinding().getKey(),
                fragment.getName().getIdentifier(),
                typeOf(fragment.resolveBinding().getType())));
        }
    }

    private List<StatementNode> readLambdaExpressionBody(LambdaExpression expression) {
        TypeName returnType = typeOf(expression.resolveTypeBinding().getFunctionalInterfaceMethod().getReturnType());
        if (expression.getBody() instanceof Block) {
            @SuppressWarnings("unchecked")
            List<Statement> statements = ((Block) expression.getBody()).statements();
            return readStatements(statements, Optional.of(returnType));
        } else {
            Expression body = (Expression) expression.getBody();
            return asList(returns(expressionReader().readExpression(returnType, body)));
        }
    }

    private class FunctionDeclaration {
        private final List<AnnotationNode> annotations;
        private final List<FormalArgumentNode> formalArguments;
        private final List<StatementNode> body;

        private FunctionDeclaration(
            List<AnnotationNode> annotations,
            List<FormalArgumentNode> formalArguments,
            List<StatementNode> body)
        {
            this.annotations = annotations;
            this.formalArguments = formalArguments;
            this.body = body;
        }

        public List<AnnotationNode> getAnnotations() {
            return annotations;
        }

        public List<FormalArgumentNode> getFormalArguments() {
            return formalArguments;
        }

        public List<StatementNode> getBody() {
            return body;
        }
    }

    private List<StatementNode> readStatements(List<Statement> body, Optional<TypeName> returnType) {
        JavaStatementReader statementReader = new JavaStatementReader(expressionReader(), returnType);
        return eagerFlatMap(body, statementReader::readStatement);
    }

    private TypeName generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        String packageName = ast.getPackage().getName().getFullyQualifiedName();
        String className = type.getName().getFullyQualifiedName();
        return TypeName.of(packageName + "." + className);
    }

    private FormalArgumentNode readSingleVariableDeclaration(SingleVariableDeclaration parameter) {
        return formalArg(var(
            parameter.resolveBinding().getKey(),
            parameter.getName().getIdentifier(),
            typeOf(parameter.resolveBinding())));
    }

    private JavaExpressionReader expressionReader() {
        return new JavaExpressionReader(this);
    }
}