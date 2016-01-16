package org.zwobble.couscous.backends.csharp;

public class CsharpWriter {
    private final StringBuilder builder = new StringBuilder();

    public String asString() {
        return builder.toString();
    }

    public void writeIntegerLiteral(int value) {
        builder.append(Integer.toString(value));
    }

    public void writeStringLiteral(String value) {
        // TODO: escaping
        builder.append("\"" + value + "\"");
    }

    public void writeBooleanLiteral(boolean value) {
        builder.append(value ? "true" : "false");
    }

    public void writeKeyword(String keyword) {
        builder.append(keyword);
    }

    public void writeSymbol(String symbol) {
        builder.append(symbol);
    }

    public void writeIdentifier(String identifier) {
        builder.append(identifier);
    }

    public void writeSpace() {
        builder.append(" ");
    }
}
