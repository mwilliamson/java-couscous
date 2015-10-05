package org.zwobble.couscous.backends.python;

import org.zwobble.couscous.backends.python.ast.PythonAssignmentNode;
import org.zwobble.couscous.backends.python.ast.PythonAttributeAccessNode;
import org.zwobble.couscous.backends.python.ast.PythonBlock;
import org.zwobble.couscous.backends.python.ast.PythonBooleanLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonCallNode;
import org.zwobble.couscous.backends.python.ast.PythonClassNode;
import org.zwobble.couscous.backends.python.ast.PythonConditionalExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonExpressionNode;
import org.zwobble.couscous.backends.python.ast.PythonFunctionDefinitionNode;
import org.zwobble.couscous.backends.python.ast.PythonIntegerLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonModuleNode;
import org.zwobble.couscous.backends.python.ast.PythonNode;
import org.zwobble.couscous.backends.python.ast.PythonPassNode;
import org.zwobble.couscous.backends.python.ast.PythonReturnNode;
import org.zwobble.couscous.backends.python.ast.PythonStringLiteralNode;
import org.zwobble.couscous.backends.python.ast.PythonVariableReferenceNode;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

import static com.google.common.collect.Iterables.skip;

import lombok.val;

public class PythonSerializer implements PythonNodeVisitor {
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
    public void visit(PythonIntegerLiteralNode integerLiteral) {
        writer.writeInteger(integerLiteral.getValue());
    }

    @Override
    public void visit(PythonStringLiteralNode stringLiteral) {
        writer.writeStringLiteral(stringLiteral.getValue());
    }

    @Override
    public void visit(PythonBooleanLiteralNode booleanLiteral) {
        writer.writeKeyword(booleanLiteral.getValue() ? "True" : "False");
    }

    @Override
    public void visit(PythonVariableReferenceNode reference) {
        writer.writeIdentifier(reference.getName());
    }

    @Override
    public void visit(PythonConditionalExpressionNode conditional) {
        writeParenthesised(conditional.getTrueValue());
        writer.writeSpace();
        writer.writeKeyword("if");
        writer.writeSpace();
        writeParenthesised(conditional.getCondition());
        writer.writeSpace();
        writer.writeKeyword("else");
        writer.writeSpace();
        writeParenthesised(conditional.getFalseValue());
    }

    @Override
    public void visit(PythonAttributeAccessNode attributeAccess) {
        writeParenthesised(attributeAccess.getLeft());
        writer.writeSymbol(".");
        writer.writeIdentifier(attributeAccess.getAttributeName());
    }

    @Override
    public void visit(PythonCallNode call) {
        writeParenthesised(call.getCallee());
        writer.writeSymbol("(");
        if (!call.getArguments().isEmpty()) {
            write(call.getArguments().get(0));
            for (val argumentName : skip(call.getArguments(), 1)) {
                writer.writeSymbol(",");
                writer.writeSpace();
                write(argumentName);
            }
        }
        writer.writeSymbol(")");
    }

    @Override
    public void visit(PythonReturnNode pythonReturn) {
        writer.writeStatement(() -> {
            writer.writeKeyword("return");
            writer.writeSpace();
            write(pythonReturn.getValue());
        });
    }

    @Override
    public void visit(PythonPassNode pass) {
        writer.writeStatement(() -> {
            writer.writeKeyword("pass");            
        });
    }

    @Override
    public void visit(PythonClassNode pythonClass) {
        writer.writeStatement(() -> {
            writer.writeKeyword("class");
            writer.writeSpace();
            writer.writeIdentifier(pythonClass.getName());
            writer.writeSymbol("(");
            writer.writeIdentifier("object");
            writer.writeSymbol(")");
            writeBlock(pythonClass.getBody());
        });
    }

    @Override
    public void visit(PythonFunctionDefinitionNode functionDefinition) {
        writer.writeStatement(() -> {
            writer.writeKeyword("def");
            writer.writeSpace();
            writer.writeIdentifier(functionDefinition.getName());
            writer.writeSymbol("(");
            writeArgumentNames(functionDefinition);
            writer.writeSymbol(")");
            writeBlock(functionDefinition.getBody());
        });
    }

    @Override
    public void visit(PythonAssignmentNode assignment) {
        writer.writeStatement(() -> {
            write(assignment.getTarget());
            writer.writeSpace();
            writer.writeSymbol("=");
            writer.writeSpace();
            write(assignment.getValue());
            
        });
    }

    private void writeParenthesised(PythonExpressionNode expression) {
        writer.writeSymbol("(");
        write(expression);
        writer.writeSymbol(")");
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
    public void visit(PythonModuleNode module) {
        for (val statement : module.getStatements()) {
            write(statement);
        }
    }

    private void writeBlock(PythonBlock body) {
        writer.startBlock();
        for (val statement : body) {
            write(statement);
        }
        writer.endBlock();
    }
}
