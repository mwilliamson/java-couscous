package org.zwobble.couscous.frontends.java;

import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ClassNodeBuilder;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TypeName;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;

public class JavaReader {
    private final JavaParser parser = new JavaParser();
    
    public List<ClassNode> readClassFromFile(Path root, Path sourcePath) throws IOException {
        CompilationUnit ast = parser.parseCompilationUnit(root, sourcePath);
        return readCompilationUnit(ast);
    }
    
    private static List<ClassNode> readCompilationUnit(CompilationUnit ast) {
        String name = generateClassName(ast);
        ClassNodeBuilder classBuilder = new ClassNodeBuilder(name);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        readFields(type, classBuilder);
        readMethods(type, classBuilder);
        return asList(classBuilder.build());
    }
    
    private static void readFields(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (FieldDeclaration field : type.getFields()) {
            readField(field, classBuilder);
        }
    }
    
    private static void readField(FieldDeclaration field, ClassNodeBuilder classBuilder) {
        for (Object fragment : field.fragments()) {
            String name = ((VariableDeclarationFragment)fragment).getName().getIdentifier();
            classBuilder.field(name, typeOf(field.getType()));
        }
    }
    
    private static void readMethods(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (MethodDeclaration method : type.getMethods()) {
            readMethod(classBuilder, method);
        }
    }
    
    private static void readMethod(ClassNodeBuilder classBuilder, MethodDeclaration method) {
        if (method.isConstructor()) {
            classBuilder.constructor(builder -> buildMethod(method, builder));
        } else {
            classBuilder.method(method.getName().getIdentifier(), true, builder -> buildMethod(method, builder));
        }
    }

    private static <T> ClassNodeBuilder.MethodBuilder<T> buildMethod(MethodDeclaration method, ClassNodeBuilder.MethodBuilder<T> builder) {
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
        JavaStatementReader statementReader = new JavaStatementReader(returnType);
        for (Object statement : method.getBody().statements()) {
            for (StatementNode intermediateStatement : statementReader.readStatement((Statement)statement)) {
                builder.statement(intermediateStatement);
            }
        }
        return builder;
    }

    private static String generateClassName(CompilationUnit ast) {
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        return ast.getPackage().getName().getFullyQualifiedName() + "." + type.getName().getFullyQualifiedName();
    }
}