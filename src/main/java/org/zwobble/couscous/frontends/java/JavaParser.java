package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;

import lombok.val;

public class JavaParser {
    private final ASTParser parser;
    
    public JavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
        parser.setBindingsRecovery(true);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        parser.setCompilerOptions(JavaCore.getOptions());
        parser.setEnvironment(
            new String[] {"/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar"},
            new String[0],
            new String[0],
            false);
        parser.setResolveBindings(true);
    }
    
    public CompilationUnit parseCompilationUnit(Path root, Path sourcePath) throws IOException {
        parser.setUnitName("/" + root.relativize(sourcePath).toString());
        
        val javaFileBytes = Files.readAllBytes(sourcePath);
        val source = Charsets.UTF_8.decode(ByteBuffer.wrap(javaFileBytes));
        parser.setSource(source.array());
        return (CompilationUnit)parser.createAST(null);
    }
}
