package org.zwobble.couscous.transforms;

import com.google.common.collect.ImmutableList;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.sugar.AnonymousClass;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.GeneratedClosure;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.types.ScalarType;

import java.util.List;
import java.util.Optional;

import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.only;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.ExtraMaps.map;
import static org.zwobble.couscous.util.ExtraSets.set;

public class AnonymousClassToInnerClass {
    public static TypeNode transform(TypeNode type) {
        Scope scope = Scope.create().temporaryPrefix("_couscous_anonymous_class_to_inner_class");
        AnonymousClassToInnerClass transformer = new AnonymousClassToInnerClass(scope, type);
        return transformer.transformTypeDeclaration(type);
    }

    private final Scope scope;
    private final TypeNode type;
    private final ImmutableList.Builder<TypeNode> innerTypes = ImmutableList.builder();
    private int anonymousClassCount = 0;

    private AnonymousClassToInnerClass(Scope scope, TypeNode type) {
        this.scope = scope;
        this.type = type;
    }

    private TypeNode transformTypeDeclaration(TypeNode type) {
        NodeTransformer transformer = NodeTransformer.builder()
            .transformExpression(this::transformExpression)
            .build();
        return only(transformer.transformTypeDeclaration(type)).addInnerTypes(innerTypes.build());
    }

    private Optional<ExpressionNode> transformExpression(ExpressionNode expression) {
        return tryCast(AnonymousClass.class, expression)
            .map(anonymous -> {
                ScalarType className = ScalarType.innerType(type.getName(), "Anonymous_" + anonymousClassCount++);
                GeneratedClosure closure = classWithCapture(scope, className, anonymous);
                innerTypes.add(closure.getClassNode());
                return closure.generateConstructor();
            });
    }

    private GeneratedClosure classWithCapture(
        Scope scope,
        ScalarType className,
        AnonymousClass anonymousClass
    ) {
        ClassNode classNode = ClassNode.declareClass(
            className,
            list(),
            set(anonymousClass.getType()),
            anonymousClass.getFields(),
            list(),
            ConstructorNode.DEFAULT,
            anonymousClass.getMethods(),
            list()
        );
        if (anonymousClass.getAnonymousType().isPresent()) {
            NodeTransformer nodeTransformer = NodeTransformer.replaceExpressions(map(
                thisReference(anonymousClass.getAnonymousType().get()),
                thisReference(className)
            ));
            classNode = nodeTransformer.transformClass(classNode);
        }
        return ClosureGenerator.classWithCapture(scope, classNode);
    }

    private List<Node> relevantDescendants(Node root) {
        return eagerFlatMap(
            eagerFilter(root.childNodes(), node -> !(node instanceof TypeNode)),
            node -> cons(node, relevantDescendants(node))
        );
    }
}
