package org.zwobble.couscous.frontends.java;

import com.google.common.collect.ImmutableList;
import org.eclipse.jdt.core.dom.*;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.ClassNodeBuilder;
import org.zwobble.couscous.ast.StatementNode;
import org.zwobble.couscous.ast.TypeName;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.frontends.java.JavaTypes.typeOf;
import static org.zwobble.couscous.util.ExtraLists.cons;

public class JavaReader {
    public static List<ClassNode> readClassFromFile(Path root, Path sourcePath) throws IOException {
        CompilationUnit ast = new JavaParser().parseCompilationUnit(root, sourcePath);
        JavaReader reader = new JavaReader();
        return reader.readCompilationUnit(ast);
    }

    private final ImmutableList.Builder<ClassNode> classes;

    private JavaReader() {
        classes = ImmutableList.builder();
    }

    private List<ClassNode> readCompilationUnit(CompilationUnit ast) {
        String name = generateClassName(ast);
        ClassNodeBuilder classBuilder = new ClassNodeBuilder(name);
        TypeDeclaration type = (TypeDeclaration)ast.types().get(0);
        readFields(type, classBuilder);
        readMethods(type, classBuilder);
        return cons(classBuilder.build(), classes.build());
    }
    
    private void readFields(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (FieldDeclaration field : type.getFields()) {
            readField(field, classBuilder);
        }
    }
    
    private void readField(FieldDeclaration field, ClassNodeBuilder classBuilder) {
        for (Object fragment : field.fragments()) {
            String name = ((VariableDeclarationFragment)fragment).getName().getIdentifier();
            classBuilder.field(name, typeOf(field.getType()));
        }
    }
    
    private void readMethods(TypeDeclaration type, ClassNodeBuilder classBuilder) {
        for (MethodDeclaration method : type.getMethods()) {
            readMethod(classBuilder, method);
        }
    }
    
    private void readMethod(ClassNodeBuilder classBuilder, MethodDeclaration method) {
        if (method.isConstructor()) {
            classBuilder.constructor(builder -> buildMethod(method, builder));
        } else {
            classBuilder.method(method.getName().getIdentifier(), true, builder -> buildMethod(method, builder));
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
        JavaStatementReader statementReader = new JavaStatementReader(new JavaExpressionReader(classes), returnType);
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
}