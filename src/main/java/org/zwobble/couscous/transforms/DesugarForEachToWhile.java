package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.JavaTypes;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.ast.LocalVariableDeclarationNode.localVariableDeclaration;
import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.ast.WhileNode.whileLoop;
import static org.zwobble.couscous.types.BoundTypeParameter.boundTypeParameter;
import static org.zwobble.couscous.util.ExtraLists.cons;
import static org.zwobble.couscous.util.ExtraLists.list;

public class DesugarForEachToWhile {
    public static NodeTransformer transformer() {
        Scope scope = Scope.create().temporaryPrefix("_couscous_desugar_foreach_to_while");
        return NodeTransformer.builder()
            .transformStatement(statement -> {
                if (statement.type() == NodeTypes.FOR_EACH) {
                    return Optional.of(desugar(scope, ((ForEachNode) statement)));
                } else {
                    return Optional.empty();
                }
            })
            .build();
    }

    public static List<StatementNode> desugar(Scope scope, ForEachNode forEach) {
        Type elementType = forEach.getTarget().getType();
        ExpressionNode iteratorValue = methodCall(forEach.getIterable(), "iterator", list(), JavaTypes.iterator(elementType));
        LocalVariableDeclarationNode iterator = scope.temporaryVariable(iteratorValue);

        ExpressionNode hasNext = methodCall(reference(iterator), "hasNext", list(), Types.BOOLEAN);
        ExpressionNode next = methodCall(reference(iterator), "next", list(), boundTypeParameter(JavaTypes.ITERATOR_TYPE_PARAMETER, elementType));
        WhileNode loop = whileLoop(hasNext, cons(
            localVariableDeclaration(forEach.getTarget(), next),
            forEach.getStatements()
        ));

        return list(new StatementBlockNode(list(iterator, loop)));
    }
}
