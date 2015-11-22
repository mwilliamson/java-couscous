package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

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
        IMethodBinding methodBinding = expression.resolveMethodBinding();
        IMethodBinding functionalInterfaceMethod = expression.resolveTypeBinding().getFunctionalInterfaceMethod();
        TypeName returnType = typeOf(functionalInterfaceMethod.getReturnType());

        ClassNodeBuilder classBuilder = new ClassNodeBuilder(generateClassName(expression));
        classBuilder.method(functionalInterfaceMethod.getName(), method -> {
            ASTNode body = expression.getBody();
            if ((body instanceof Expression)) {
                method.statement(returns(expressionReader().readExpression(
                    returnType,
                    (Expression)body)));
            } else {
                throw new UnsupportedOperationException();
            }
            return method;
        });

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
        if (method.isConstructor()) {
            classBuilder.constructor(builder -> buildMethod(method, builder));
        } else {
            classBuilder.method(
                method.getName().getIdentifier(),
                Modifier.isStatic(method.getModifiers()),
                builder -> buildMethod(method, builder));
        }
    }

    private <T> ClassNodeBuilder.MethodBuilder<T> buildMethod(MethodDeclaration method, ClassNodeBuilder.MethodBuilder<T> builder) {
        for (IAnnotationBinding annotation : method.resolveBinding().getAnnotations()) {
            builder.annotation(typeOf(annotation.getAnnotationType()));
        }
        for (Object parameterObject : method.parameters()) {
            SingleVariableDeclaration parameter = (SingleVariableDeclaration)parameterObject;
            builder.argument(parameter.resolveBinding().getKey(), parameter.getName().getIdentifier(), typeOf(parameter.resolveBinding()));
        }
        Optional<TypeName> returnType = method.getReturnType2() == null
            ? Optional.empty()
            : Optional.of(typeOf(method.getReturnType2()));
        JavaStatementReader statementReader = new JavaStatementReader(expressionReader(), returnType);
        for (Object statement : method.getBody().statements()) {
            for (StatementNode intermediateStatement : statementReader.readStatement((Statement)statement)) {
                builder.statement(intermediateStatement);
            }
        }
        return builder;
    }

    private String generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }

    private JavaExpressionReader expressionReader() {
        return new JavaExpressionReader(this);
    }
}