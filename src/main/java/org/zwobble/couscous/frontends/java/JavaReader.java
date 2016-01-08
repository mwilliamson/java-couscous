package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
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
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;
import static org.zwobble.couscous.frontends.java.JavaTypes.*;
import static org.zwobble.couscous.util.ExtraLists.*;

public class JavaReader {
    public static List<ClassNode> readClassFromFile(List<Path> sourcePaths, Path sourcePath) throws IOException {
        CompilationUnit ast = new JavaParser().parseCompilationUnit(sourcePaths, sourcePath);
        System.out.println(sourcePath);
        for (Message message : ast.getMessages()) {
            System.out.println(message.getMessage());
        }
        JavaReader reader = new JavaReader();
        return cons(reader.readCompilationUnit(ast), reader.classes.build());
    }

    private final ImmutableList.Builder<ClassNode> classes;
    private int anonymousClassCount = 0;

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private ClassNode readCompilationUnit(CompilationUnit ast) {
        TypeName name = generateClassName(ast);
        Scope scope = Scope.create().enterClass(name);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        TypeDeclarationBody body = readTypeDeclarationBody(scope, type.bodyDeclarations());
        return ClassNode.declareClass(
            name,
            superTypes(type),
            body.getFields(),
            body.getConstructor(),
            body.getMethods());
    }

    GeneratedClosure readExpressionMethodReference(Scope outerScope, ExpressionMethodReference expression) {
        TypeName name = generateAnonymousName(expression);
        Scope scope = outerScope.enterClass(name);
        return generateClosure(
            scope,
            name,
            expression.resolveTypeBinding().getFunctionalInterfaceMethod(),
            new JavaExpressionMethodReferenceReader(this).toLambda(scope, expression));
    }

    GeneratedClosure readLambda(Scope outerScope, LambdaExpression expression) {
        TypeName name = generateAnonymousName(expression);
        Scope scope = outerScope.enterClass(name);
        return generateClosure(
            scope,
            name,
            expression.resolveTypeBinding().getFunctionalInterfaceMethod(),
            new JavaLambdaExpressionReader(this).toLambda(scope, expression));
    }

    GeneratedClosure generateClosure(Scope scope, TypeName name, IMethodBinding functionalInterfaceMethod, Lambda lambda) {
        MethodNode method = MethodNode.method(
            emptyList(),
            false,
            functionalInterfaceMethod.getName(),
            lambda.getFormalArguments(),
            lambda.getBody());

        GeneratedClosure closure = classWithCapture(
            scope,
            name,
            superTypesAndSelf(functionalInterfaceMethod.getDeclaringClass()),
            emptyList(),
            list(method));

        classes.add(closure.getClassNode());
        return closure;
    }

    private List<StatementNode> replaceCaptureReferences(
        TypeName className,
        List<StatementNode> body,
        List<CapturedVariable> freeVariables)
    {
        Map<ExpressionNode, ExpressionNode> replacements = Maps.transformValues(
            Maps.uniqueIndex(freeVariables, variable -> variable.freeVariable),
            freeVariable -> captureAccess(className, freeVariable));
        Function<ExpressionNode, ExpressionNode> replaceExpression = new CaptureReplacer(replacements);
        return eagerMap(body, statement -> statement.replaceExpressions(replaceExpression));
    }

    private class CaptureReplacer implements Function<ExpressionNode, ExpressionNode> {
        private final Map<ExpressionNode, ExpressionNode> replacements;

        public CaptureReplacer(Map<ExpressionNode, ExpressionNode> replacements) {
            this.replacements = replacements;
        }

        @Override
        public ExpressionNode apply(ExpressionNode expression) {
            return Optional.ofNullable(replacements.get(expression))
                .orElseGet(() -> expression.replaceExpressions(this));
        }
    }

    private ConstructorNode buildConstructor(Scope outerScope, TypeName type, List<CapturedVariable> freeVariables) {
        Scope scope = outerScope.enterConstructor();

        ImmutableList.Builder<FormalArgumentNode> arguments = ImmutableList.builder();
        ImmutableList.Builder<StatementNode> body = ImmutableList.builder();

        for (CapturedVariable variable : freeVariables) {
            FormalArgumentNode argument = scope.formalArgument(variable.field.getName(), variable.field.getType());
            arguments.add(argument);
            body.add(assignStatement(captureAccess(type, variable), reference(argument)));
        }

        return constructor(arguments.build(), body.build());
    }

    private FieldAccessNode captureAccess(TypeName type, CapturedVariable freeVariable) {
        FieldDeclarationNode field = freeVariable.field;
        return fieldAccess(thisReference(type), field.getName(), field.getType());
    }

    GeneratedClosure readAnonymousClass(Scope outerScope, AnonymousClassDeclaration declaration) {
        TypeName className = generateAnonymousName(declaration);
        Scope scope = outerScope.enterClass(className);
        TypeDeclarationBody bodyDeclarations = readTypeDeclarationBody(scope, declaration.bodyDeclarations());
        GeneratedClosure closure = classWithCapture(
            scope,
            className,
            superTypes(declaration),
            bodyDeclarations.getFields(),
            bodyDeclarations.getMethods());
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
        TypeName className,
        Set<TypeName> superTypes,
        List<FieldDeclarationNode> declaredFields,
        List<MethodNode> methods
    ) {
        List<ReferenceNode> freeVariables = findFreeVariables(ExtraLists.concat(declaredFields, methods));
        List<CapturedVariable> capturedVariables = ImmutableList.copyOf(transform(
            freeVariables,
            freeVariable -> new CapturedVariable(freeVariable, fieldForCapture(freeVariable))));
        Iterable<FieldDeclarationNode> captureFields = transform(capturedVariables, capture -> capture.field);

        List<FieldDeclarationNode> fields = ImmutableList.copyOf(concat(declaredFields, captureFields));

        ClassNode classNode = ClassNode.declareClass(
            className,
            superTypes,
            fields,
            buildConstructor(scope, className, capturedVariables),
            eagerMap(methods, method ->
                method.mapBody(body -> replaceCaptureReferences(className, body, capturedVariables))));
        return new GeneratedClosure(classNode, freeVariables);
    }

    private FieldDeclarationNode fieldForCapture(ReferenceNode freeVariable) {
        return freeVariable.accept(new ReferenceNode.Visitor<FieldDeclarationNode>() {
            @Override
            public FieldDeclarationNode visit(VariableReferenceNode reference) {
                return field(reference.getReferent().getName(), reference.getType());
            }

            @Override
            public FieldDeclarationNode visit(ThisReferenceNode thisReference) {
                TypeName type = thisReference.getType();
                String name = "this_" + type.getQualifiedName().replace(".", "__");
                return field(name, type);
            }
        });
    }

    private TypeName generateAnonymousName(ASTNode node) {
        ITypeBinding type = findDeclaringClass(node);
        while (type.isAnonymous()) {
            type = type.getDeclaringClass();
        }
        return TypeName.of(type.getQualifiedName() + "_Anonymous_" + (anonymousClassCount++));
    }

    private ITypeBinding findDeclaringClass(ASTNode node) {
        while (!(node instanceof AbstractTypeDeclaration)) {
            node = node.getParent();
        }
        return ((AbstractTypeDeclaration)node).resolveBinding();
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

    private TypeDeclarationBody readTypeDeclarationBody(Scope scope, List<Object> bodyDeclarations) {
        ImmutableList.Builder<MethodNode> methods = ImmutableList.builder();
        ConstructorNode constructor = ConstructorNode.DEFAULT;

        for (CallableNode callable : readMethods(scope, ofType(bodyDeclarations, MethodDeclaration.class))) {
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

    private List<CallableNode> readMethods(Scope scope, List<MethodDeclaration> methods) {
        return eagerMap(methods, method -> readMethod(scope, method));
    }

    private CallableNode readMethod(Scope outerScope, MethodDeclaration method) {
        Scope scope = outerScope.enterMethod(method.getName().getIdentifier());

        List<FormalArgumentNode> formalArguments = readFormalArguments(scope, method);
        List<StatementNode> body = readBody(scope, method);
        List<AnnotationNode> annotations = readAnnotations(method);

        if (method.isConstructor()) {
            return constructor(
                formalArguments,
                body);
        } else {
            return MethodNode.method(
                annotations,
                Modifier.isStatic(method.getModifiers()),
                method.getName().getIdentifier(),
                formalArguments,
                body);
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

    private List<StatementNode> readBody(Scope scope, MethodDeclaration method) {
        @SuppressWarnings("unchecked")
        List<Statement> statements = method.getBody().statements();
        Optional<TypeName> returnType = Optional.ofNullable(method.getReturnType2())
            .map(Type::resolveBinding)
            .map(JavaTypes::typeOf);
        return readStatements(scope, statements, returnType);
    }

    List<StatementNode> readStatements(Scope scope, List<Statement> body, Optional<TypeName> returnType) {
        JavaStatementReader statementReader = new JavaStatementReader(scope, expressionReader(scope), returnType);
        return eagerFlatMap(body, statementReader::readStatement);
    }

    ExpressionNode readExpression(Scope scope, TypeName targetType, Expression body) {
        return expressionReader(scope).readExpression(targetType, body);
    }

    ExpressionNode readExpressionWithoutBoxing(Scope scope, Expression body) {
        return expressionReader(scope).readExpressionWithoutBoxing(body);
    }

    private TypeName generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        String packageName = ast.getPackage().getName().getFullyQualifiedName();
        String className = type.getName().getFullyQualifiedName();
        return TypeName.of(packageName + "." + className);
    }

    private JavaExpressionReader expressionReader(Scope scope) {
        return new JavaExpressionReader(scope, this);
    }
}