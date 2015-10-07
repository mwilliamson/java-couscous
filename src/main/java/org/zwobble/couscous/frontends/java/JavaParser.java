package org.zwobble.couscous.frontends.java;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;

import lombok.SneakyThrows;
import lombok.val;

public class JavaParser {
    private final ASTParser parser;
    
    public JavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
    }
    
    @SneakyThrows
    public CompilationUnit parseCompilationUnit(Path path) {
        val javaFileBytes = Files.readAllBytes(path);
        val javaFileChars = Charsets.UTF_8.decode(ByteBuffer.wrap(javaFileBytes));
        parser.setSource(javaFileChars.array());
        return (CompilationUnit)parser.createAST(null);
    }
}
