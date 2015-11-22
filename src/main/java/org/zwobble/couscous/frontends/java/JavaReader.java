package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.VariableDeclaration;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.cons;
import static org.zwobble.couscous.util.ExtraLists.ofType;

public class JavaReader {
    public static List<ClassNode> readClassFromFile(Path root, Path sourcePath) throws IOException {
        CompilationUnit ast = new JavaParser().parseCompilationUnit(root, sourcePath);
        JavaReader reader = new JavaReader();
        return cons(reader.readCompilationUnit(ast), reader.classes.build());
    }

    private final ImmutableList.Builder<ClassNode> classes;
    private int anonymousClassCount = 0;

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private ClassNode readCompilationUnit(CompilationUnit ast) {
        String name = generateClassName(ast);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        return readTypeDeclarationBody(name, type.bodyDeclarations());
    }

    TypeName readLambda(LambdaExpression expression) {
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();

        ClassNodeBuilder classBuilder = new ClassNodeBuilder(generateClassName(expression));
        classBuilder.method(functionalInterfaceMethod.getName(), method -> buildMethod(new LambdaDeclarationAdaptor(expression), method));

        ClassNode classNode = classBuilder.build();
        classes.add(classNode);
        return classNode.getName();
    }

    private String generateClassName(LambdaExpression expression) {
        return generateClassName(expression.resolveMethodBinding().getDeclaringClass());
    }

    TypeName readAnonymousClass(AnonymousClassDeclaration declaration) {
        String name = generateClassName(declaration);
        ClassNode classNode = readTypeDeclarationBody(name, declaration.bodyDeclarations());
        classes.add(classNode);
        return classNode.getName();
    }

    private String generateClassName(AnonymousClassDeclaration declaration) {
        ITypeBinding type = declaration.resolveBinding();
        return generateClassName(type);
    }

    private String generateClassName(ITypeBinding type) {
        while (type.isAnonymous()) {
            type = type.getDeclaringClass();
        }
        return type.getQualifiedName() + "_Anonymous_" + (anonymousClassCount++);
    }

    private ClassNode readTypeDeclarationBody(String name, List bodyDeclarations) {
        ClassNodeBuilder classBuilder = new ClassNodeBuilder(name);
        readFields(ofType(bodyDeclarations, FieldDeclaration.class), classBuilder);
        readMethods(ofType(bodyDeclarations, MethodDeclaration.class), classBuilder);
        return classBuilder.build();
    }

    private void readFields(List<FieldDeclaration> fields, ClassNodeBuilder classBuilder) {
        for (FieldDeclaration field : fields) {
            readField(field, classBuilder);
        }
    }

    private void readField(FieldDeclaration field, ClassNodeBuilder classBuilder) {
        for (Object fragment : field.fragments()) {
            String name = ((VariableDeclarationFragment)fragment).getName().getIdentifier();
            classBuilder.field(name, typeOf(field.getType()));
        }
    }

    private void readMethods(List<MethodDeclaration> methods, ClassNodeBuilder classBuilder) {
        for (MethodDeclaration method : methods) {
            readMethod(classBuilder, method);
        }
    }

    private void readMethod(ClassNodeBuilder classBuilder, MethodDeclaration method) {
        MethodDeclarationAdaptor function = new MethodDeclarationAdaptor(method);
        if (method.isConstructor()) {
            classBuilder.constructor(builder -> buildMethod(function, builder));
        } else {
            classBuilder.method(
                method.getName().getIdentifier(),
                Modifier.isStatic(method.getModifiers()),
                builder -> buildMethod(function, builder));
        }
    }

    private interface FunctionDeclaration {
        List<IAnnotationBinding> getAnnotations();
        Stream<FormalArgumentNode> getFormalArguments();
        // TODO: add tests around void methods
        Stream<StatementNode> getBody();
    }

    private class MethodDeclarationAdaptor implements FunctionDeclaration {
        private final MethodDeclaration method;

        public MethodDeclarationAdaptor(MethodDeclaration method) {
            this.method = method;
        }

        @Override
        public List<IAnnotationBinding> getAnnotations() {
            return asList(method.resolveBinding().getAnnotations());
        }

        @Override
        public Stream<FormalArgumentNode> getFormalArguments() {
            @SuppressWarnings("unchecked")
            List<SingleVariableDeclaration> parameters = method.parameters();
            return parameters.stream().map(parameter -> readSingleVariableDeclaration(parameter));
        }

        @Override
        public Stream<StatementNode> getBody() {
            @SuppressWarnings("unchecked")
            List<Statement> statements = method.getBody().statements();
            return readStatements(statements, getReturnType());
        }

        private Optional<TypeName> getReturnType() {
            return Optional.ofNullable(method.getReturnType2())
                .map(Type::resolveBinding)
                .map(JavaTypes::typeOf);
        }
    }

    private class LambdaDeclarationAdaptor implements FunctionDeclaration {
        private final LambdaExpression expression;

        public LambdaDeclarationAdaptor(LambdaExpression expression) {
            this.expression = expression;
        }

        @Override
        public List<IAnnotationBinding> getAnnotations() {
            return asList(expression.resolveMethodBinding().getAnnotations());
        }

        @Override
        public Stream<FormalArgumentNode> getFormalArguments() {
            return ((List<?>)expression.parameters()).stream()
                .map(this::readParameter);
        }

        private FormalArgumentNode readParameter(Object parameter) {
            if (parameter instanceof SingleVariableDeclaration) {
                return readSingleVariableDeclaration((SingleVariableDeclaration) parameter);
            } else {
                VariableDeclarationFragment fragment = (VariableDeclarationFragment)parameter;
                return FormalArgumentNode.formalArg(VariableDeclaration.var(
                    fragment.resolveBinding().getKey(),
                    fragment.getName().getIdentifier(),
                    typeOf(fragment.resolveBinding().getType())));
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Stream<StatementNode> getBody() {
            TypeName returnType = typeOf(getReturnType());
            if (expression.getBody() instanceof Block) {
                @SuppressWarnings("unchecked")
                List<Statement> statements = ((Block) expression.getBody()).statements();
                return readStatements(statements, Optional.of(returnType));
            } else {
                Expression expression = (Expression) this.expression.getBody();
                return Stream.of(returns(expressionReader().readExpression(returnType, expression)));
            }
        }

        private ITypeBinding getReturnType() {
            return expression.resolveTypeBinding().getFunctionalInterfaceMethod().getReturnType();
        }
    }

    private <T> ClassNodeBuilder.MethodBuilder<T> buildMethod(FunctionDeclaration method, ClassNodeBuilder.MethodBuilder<T> builder) {
        for (IAnnotationBinding annotation : method.getAnnotations()) {
            builder.annotation(typeOf(annotation.getAnnotationType()));
        }
        method.getFormalArguments().forEach(builder::argument);
        method.getBody().forEach(builder::statement);
        return builder;
    }

    private Stream<StatementNode> readStatements(List<Statement> body, Optional<TypeName> returnType) {
        JavaStatementReader statementReader = new JavaStatementReader(expressionReader(), returnType);
        return body.stream()
            .flatMap(statement -> statementReader.readStatement(statement).stream());
    }

    private String generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }

    private FormalArgumentNode readSingleVariableDeclaration(SingleVariableDeclaration parameter) {
        return FormalArgumentNode.formalArg(VariableDeclaration.var(
            parameter.resolveBinding().getKey(),
            parameter.getName().getIdentifier(),
            typeOf(parameter.resolveBinding())));
    }

    private JavaExpressionReader expressionReader() {
        return new JavaExpressionReader(this);
    }
}