package org.zwobble.couscous.backends.python.ast;

import com.google.common.base.Strings;

public class PythonWriter {
    private static final int SPACES_PER_INDENT = 4;
    private final StringBuilder builder = new StringBuilder();
    private int depth = 0;
    
    public String asString() {
        return builder.toString();
    }

    public void writeInteger(int value) {
        builder.append(value);
    }

    public void writeStringLiteral(String value) {
        builder.append("\"");
        builder.append(value);
        builder.append("\"");
    }

    public void writeKeyword(String keyword) {
        builder.append(keyword);
    }

    public void writeSpace() {
        builder.append(" ");
    }

    public void writeIdentifier(String name) {
        builder.append(name);
    }

    public void writeSymbol(String symbol) {
        builder.append(symbol);
    }

    public void startBlock() {
        depth++;
        writeSymbol(":");
        builder.append("\n");
    }

    public void endBlock() {
        depth--;
    }

    public void startStatement() {
        builder.append(Strings.repeat(" ", depth * SPACES_PER_INDENT));
    }

    public void endStatement() {
        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append("\n");   
        }
    }

}
