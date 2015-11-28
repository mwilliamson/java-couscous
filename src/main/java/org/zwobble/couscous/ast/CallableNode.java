package org.zwobble.couscous.ast;

import java.util.List;

public interface CallableNode extends Node {
    List<FormalArgumentNode> getArguments();
    List<StatementNode> getBody();
}
