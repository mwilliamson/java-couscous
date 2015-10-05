package org.zwobble.couscous.backends.python;

import org.zwobble.couscous.backends.python.ast.PythonBlock;
import org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonNode;
import org.zwobble.couscous.backends.python.ast.PythonPassNode;
import org.zwobble.couscous.backends.python.ast.PythonReturnNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonWriter;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import static com.google.common.collect.Iterables.skip;

import lombok.val;

public class PythonSerializer implements PythonNodeVisitor<Void> {
    public static String serialize(PythonNode node) {
        val writer = new PythonWriter();
        val serializer = new PythonSerializer(writer);
        serializer.write(node);
        return writer.asString();
    }
    
    private PythonWriter writer;
    
    private PythonSerializer(PythonWriter writer) {
        this.writer = writer;
    }
    
    public void write(PythonNode node) {
        node.accept(this);
    }
    
    @Override
    public Void visit(PythonIntegerLiteralNode integerLiteral) {
        writer.writeInteger(integerLiteral.getValue());
        return null;
    }

    @Override
    public Void visit(PythonStringLiteralNode stringLiteral) {
        writer.writeStringLiteral(stringLiteral.getValue());
        return null;
    }

    @Override
    public Void visit(PythonBooleanLiteralNode booleanLiteral) {
        writer.writeKeyword(booleanLiteral.getValue() ? "True" : "False");
        return null;
    }

    @Override
    public Void visit(PythonReturnNode pythonReturn) {
        writer.startStatement();
        writer.writeKeyword("return");
        writer.writeSpace();
        write(pythonReturn.getValue());
        writer.endStatement();
        return null;
    }

    @Override
    public Void visit(PythonPassNode pass) {
        writer.startStatement();
        writer.writeKeyword("pass");
        writer.endStatement();
        return null;
    }

    @Override
    public Void visit(PythonClassNode pythonClass) {
        writer.startStatement();
        writer.writeKeyword("class");
        writer.writeSpace();
        writer.writeIdentifier(pythonClass.getName());
        writer.writeSymbol("(");
        writer.writeIdentifier("object");
        writer.writeSymbol(")");
        writeBlock(pythonClass.getBody());
        writer.endStatement();
        return null;
    }

    @Override
    public Void visit(PythonFunctionDefinitionNode functionDefinition) {
        writer.startStatement();
        writer.writeKeyword("def");
        writer.writeSpace();
        writer.writeIdentifier(functionDefinition.getName());
        writer.writeSymbol("(");
        writeArgumentNames(functionDefinition);
        writer.writeSymbol(")");
        writeBlock(functionDefinition.getBody());
        writer.endStatement();
        return null;
    }

    private void writeArgumentNames(PythonFunctionDefinitionNode functionDefinition) {
        if (!functionDefinition.getArgumentNames().isEmpty()) {
            writer.writeIdentifier(functionDefinition.getArgumentNames().get(0));
            for (val argumentName : skip(functionDefinition.getArgumentNames(), 1)) {
                writer.writeSymbol(",");
                writer.writeSpace();
                writer.writeIdentifier(argumentName);
            }
        }
    }

    @Override
    public Void visit(PythonModuleNode module) {
        for (val statement : module.getStatements()) {
            write(statement);
        }
        return null;
    }

    private void writeBlock(PythonBlock body) {
        writer.startBlock();
        for (val statement : body) {
            write(statement);
        }
        writer.endBlock();
    }
}
