package org.zwobble.couscous.backends;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import org.zwobble.couscous.util.Action;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Consumer;

public class SourceCodeWriter {
    public interface WriterAction {
        void run(SourceCodeWriter writer);
    }

    private static final int SPACES_PER_INDENT = 4;
    private final StringBuilder builder = new StringBuilder();

    private final WriterAction blockStart;
    private final WriterAction blockEnd;
    private final Set<String> reservedIdentifiers;
    private final Function<String, String> mangleReservedIdentifier;
    private int depth = 0;

    public SourceCodeWriter(WriterAction blockStart, WriterAction blockEnd, Set<String> reservedIdentifiers, Function<String, String> mangleReservedIdentifier) {
        this.blockStart = blockStart;
        this.blockEnd = blockEnd;
        this.reservedIdentifiers = reservedIdentifiers;
        this.mangleReservedIdentifier = mangleReservedIdentifier;
    }
    
    public String asString() {
        return builder.toString();
    }

    public void writeInteger(int value) {
        builder.append(value);
    }

    public void writeChar(char value) {
        builder.append("'");
        if (value == '\'') {
            builder.append("\\");
        }
        builder.append(value).append("'");
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
        String nameToWrite = reservedIdentifiers.contains(name)
            ? mangleReservedIdentifier.apply(name)
            : name;
        builder.append(nameToWrite);
    }

    public void writeSymbol(String symbol) {
        builder.append(symbol);
    }

    public void startBlock() {
        runAction(blockStart);
        indent();
        builder.append("\n");
    }

    public void endBlock() {
        dedent();
        runAction(blockEnd);
    }

    public void indent() {
        depth++;
    }

    public void dedent() {
        depth--;
    }

    public void writeStatement(Action action) {
        startStatement();
        action.run();
        endStatement();
    }

    private void startStatement() {
        writeIndentation();
    }

    public void writeIndentation() {
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

    public <T> void writeCommaSeparated(Iterable<T> values, Consumer<T> writeValue) {
        writeWithSeparator(values, writeValue, () -> {
            writeSymbol(",");
            writeSpace();
        });
    }

    public <T> void writeWithSeparator(Iterable<T> values, Consumer<T> writeValue, Action separator) {
        Iterator<T> iterator = values.iterator();
        if (iterator.hasNext()) {
            writeValue.accept(iterator.next());
            while(iterator.hasNext()) {
                separator.run();
                writeValue.accept(iterator.next());
            }
        }
    }
}
