package org.zwobble.couscous.backends.python.ast;

import org.zwobble.couscous.backends.python.ast.visitors.PythonNodeVisitor;

public interface PythonNode {
    void accept(PythonNodeVisitor visitor);
}
