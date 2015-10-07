package org.zwobble.couscous.frontends.java;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import com.google.common.base.Charsets;

import lombok.val;

public class JavaParser {
    private final ASTParser parser;
    
    public JavaParser() {
        parser = ASTParser.newParser(AST.JLS8);
        parser.setResolveBindings(true);
    }
    
    public CompilationUnit parseCompilationUnit(Path path) throws IOException {
        val javaFileBytes = Files.readAllBytes(path);
        val javaFileChars = Charsets.UTF_8.decode(ByteBuffer.wrap(javaFileBytes));
        return parseCompilationUnit(javaFileChars.array());
    }
    
    public CompilationUnit parseCompilationUnit(String source) {
        return parseCompilationUnit(source.toCharArray());
    }
    
    private CompilationUnit parseCompilationUnit(char[] source) {
        parser.setSource(source);
        return (CompilationUnit)parser.createAST(null);
    }
}
