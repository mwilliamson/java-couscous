package org.zwobble.couscous.ast;

import org.zwobble.couscous.ast.visitors.StatementNodeVisitor;

public interface StatementNode {
    <T> T accept(StatementNodeVisitor<T> visitor); 
}
