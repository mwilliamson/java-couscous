package org.zwobble.couscous.backends.python;

import java.util.List;
import java.util.function.Consumer;

import org.zwobble.couscous.backends.python.ast.*;
import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;
import org.zwobble.couscous.util.Action;
import static com.google.common.collect.Iterables.skip;

public class PythonSerializer implements PythonNodeVisitor {
    public static String serialize(PythonNode node) {
        PythonWriter writer = new PythonWriter();
        PythonSerializer serializer = new PythonSerializer(writer);
        serializer.write(node);
        return writer.asString();
    }
    
    private final PythonWriter writer;
    
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
        writeWithSeparator(call.getArguments(), this::write, () -> {
            writer.writeSymbol(",");
            writer.writeSpace();
        });
        writer.writeSymbol(")");
    }
    
    @Override
    public void visit(PythonGetSliceNode getSlice) {
        writeParenthesised(getSlice.getReceiver());
        writer.writeSymbol("[");
        writeWithSeparator(getSlice.getArguments(), this::write, () -> {
            writer.writeSymbol(":");
        });
        writer.writeSymbol("]");
    }

    @Override
    public void visit(PythonNotNode notOperation) {
        writer.writeKeyword("not");
        writer.writeSpace();
        writeParenthesised(notOperation.getOperand());
    }

    @Override
    public void visit(PythonBinaryOperation operation) {
        writeParenthesised(operation.getLeft());
        writer.writeSpace();
        writer.writeKeyword(operation.getOperator());
        writer.writeSpace();
        writeParenthesised(operation.getRight());
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
    
    @Override
    public void visit(PythonImportNode importNode) {
        writer.writeStatement(() -> {
            writer.writeKeyword("from");
            writer.writeSpace();
            writer.writeIdentifier(importNode.getModuleName());
            writer.writeSpace();
            writer.writeKeyword("import");
            writer.writeSpace();
            writeWithSeparator(importNode.getAliases(), alias -> {
                writer.writeIdentifier(alias.getName());
            }, () -> {
                writer.writeSymbol(",");
                writer.writeSpace();
            });
        });
    }

    @Override
    public void visit(PythonIfStatementNode ifStatement) {
        writer.writeStatement(() -> {
            writer.writeKeyword("if");
            writer.writeSpace();
            write(ifStatement.getCondition());
            writeBlock(ifStatement.getTrueBranch());
        });
        writer.writeStatement(() -> {
            writer.writeKeyword("else");
            writeBlock(ifStatement.getFalseBranch()); 
        });
    }

    @Override
    public void visit(PythonWhileNode whileLoop) {
        writer.writeStatement(() -> {
            writer.writeKeyword("while");
            writer.writeSpace();
            write(whileLoop.getCondition());
            writeBlock(whileLoop.getBody());
        });
    }

    private void writeParenthesised(PythonExpressionNode expression) {
        writer.writeSymbol("(");
        write(expression);
        writer.writeSymbol(")");
    }
    
    private void writeArgumentNames(PythonFunctionDefinitionNode functionDefinition) {
        writeWithSeparator(functionDefinition.getArgumentNames(), writer::writeIdentifier, () -> {
            writer.writeSymbol(",");
            writer.writeSpace();
        });
    }
    
    private <T> void writeWithSeparator(List<T> values, Consumer<T> writeValue, Action separator) {
        if (!values.isEmpty()) {
            writeValue.accept(values.get(0));
            for (T value : skip(values, 1)) {
                separator.run();
                writeValue.accept(value);
            }
        }
    }
    
    @Override
    public void visit(PythonModuleNode module) {
        for (PythonStatementNode statement : module.getStatements()) {
            write(statement);
        }
    }
    
    private void writeBlock(PythonBlock body) {
        writer.startBlock();
        for (PythonStatementNode statement : body) {
            write(statement);
        }
        writer.endBlock();
    }
}