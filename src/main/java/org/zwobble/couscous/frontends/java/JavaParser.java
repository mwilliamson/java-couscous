package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import com.google.common.base.Charsets;

public class JavaParser {
    private final ASTParser parser;
    
    public JavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
        parser.setBindingsRecovery(false);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        @SuppressWarnings("unchecked")
        Hashtable<String, String> options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        parser.setCompilerOptions(options);
        parser.setResolveBindings(true);
    }
    
    public CompilationUnit parseCompilationUnit(Path root, Path sourcePath) throws IOException {
        parser.setUnitName("/" + root.relativize(sourcePath).toString());
        parser.setEnvironment(
            new String[]{"/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar"},
            new String[]{"/usr/lib/jvm/java-8-openjdk-amd64/jre/src.zip",  root.toString()},
            new String[]{"UTF-8", "UTF-8"},
            false);
        final byte[] javaFileBytes = Files.readAllBytes(sourcePath);
        CharBuffer source = Charsets.UTF_8.decode(ByteBuffer.wrap(javaFileBytes));
        parser.setSource(source.array());
        return (CompilationUnit)parser.createAST(null);
    }
}