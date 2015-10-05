package org.zwobble.couscous.backends.python.ast;

import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;

import static java.util.Arrays.asList;

public class PythonBlock implements Iterable<PythonStatementNode> {
    private final List<PythonStatementNode> statements;

    public PythonBlock(List<? extends PythonStatementNode> statements) {
        if (statements.isEmpty()) {
            this.statements = asList(PythonPassNode.PASS);
        } else {
            this.statements = ImmutableList.copyOf(statements);   
        }
    }

    @Override
    public Iterator<PythonStatementNode> iterator() {
        return statements.iterator();
    }
}
