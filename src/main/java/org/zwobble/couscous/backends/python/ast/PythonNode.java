package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public interface PythonNode {
    <T> T accept(PythonNodeVisitor<T> visitor);
}
