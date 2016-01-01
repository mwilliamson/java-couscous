package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.StatementNode;

import java.util.List;

public class Lambda {
    public static Lambda lambda(List<FormalArgumentNode> formalArguments, List<StatementNode> body) {
        return new Lambda(formalArguments, body);
    }

    private final List<FormalArgumentNode> formalArguments;
    private final List<StatementNode> body;

    private Lambda(List<FormalArgumentNode> formalArguments, List<StatementNode> body) {
        this.formalArguments = formalArguments;
        this.body = body;
    }

    public List<FormalArgumentNode> getFormalArguments() {
        return formalArguments;
    }

    public List<StatementNode> getBody() {
        return body;
    }
}
