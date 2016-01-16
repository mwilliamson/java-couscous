package org.zwobble.couscous.backends;

import com.google.common.base.Strings;
import org.zwobble.couscous.util.Action;

public class SourceCodeWriter {
    public interface WriterAction {
        void run(SourceCodeWriter writer);
    }

    private static final int SPACES_PER_INDENT = 4;
    private final StringBuilder builder = new StringBuilder();

    private final WriterAction blockStart;
    private final WriterAction blockEnd;
    private int depth = 0;

    public SourceCodeWriter(WriterAction blockStart, WriterAction blockEnd) {
        this.blockStart = blockStart;
        this.blockEnd = blockEnd;
    }
    
    public String asString() {
        return builder.toString();
    }

    public void writeInteger(int value) {
        builder.append(value);
    }

    public void writeStringLiteral(String value) {
        builder.append("\"");
        builder.append(value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t"));
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
        runAction(blockStart);
        depth++;
        builder.append("\n");
    }

    public void endBlock() {
        depth--;
        runAction(blockEnd);
    }

    public void writeStatement(Action action) {
        startStatement();
        action.run();
        endStatement();
    }

    private void startStatement() {
        builder.append(Strings.repeat(" ", depth * SPACES_PER_INDENT));
    }

    private void endStatement() {
        if (builder.charAt(builder.length() - 1) != '\n') {
            builder.append("\n");   
        }
    }

    private void runAction(WriterAction action) {
        action.run(this);
    }
}
