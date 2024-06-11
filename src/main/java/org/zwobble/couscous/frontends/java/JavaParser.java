package org.zwobble.couscous.frontends.java;

import com.google.common.collect.Iterables;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.List;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static org.zwobble.couscous.util.ExtraLists.list;

public class JavaParser {
    private final ASTParser parser;
    private final String javaDir;

    public JavaParser() {
        parser = ASTParser.newParser(AST.JLS8);

        String java = System.getenv("JAVA_HOME");
        if (java == null || java.isEmpty()) {
            java = "/usr/lib/jvm/temurin-8-jdk-amd64";
        }

        System.out.println("Using JAVA_HOME: '" + java + "'");
        javaDir = java;
    }

    public CompilationUnit parseCompilationUnit(List<Path> sourcePaths, Path sourcePath) throws IOException {
        parser.setBindingsRecovery(false);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        @SuppressWarnings("unchecked")
        Hashtable<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        parser.setCompilerOptions(options);
        parser.setResolveBindings(true);
        parser.setUnitName(sourcePath.toString());

        String[] sourcePathArguments = Iterables.toArray(
            concat(
                list(jdkPath("src.zip")),
                transform(sourcePaths, Object::toString)),
            String.class);

        parser.setEnvironment(
            new String[]{jdkPath("jre/lib/rt.jar")},
            sourcePathArguments,
            Iterables.toArray(transform(asList(sourcePathArguments), argument -> "UTF-8"), String.class),
            false);
        final byte[] javaFileBytes = Files.readAllBytes(sourcePath);
        parser.setSource(new String(javaFileBytes, "UTF-8").toCharArray());
        return (CompilationUnit)parser.createAST(null);
    }

    private String jdkPath(String relativePath) {
        Path fullPath = Paths.get(javaDir, relativePath);
        if (!fullPath.toFile().exists()) {
            throw new RuntimeException("Missing JRE file: " + fullPath);
        }
        return fullPath.toString();
    }
}
