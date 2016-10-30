package org.zwobble.couscous.transforms;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.GeneratedClosure;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.TypeParameter;
import org.zwobble.couscous.util.ExtraLists;
import org.zwobble.couscous.util.InsertionOrderSet;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static org.zwobble.couscous.ast.AssignmentNode.assignStatement;
import static org.zwobble.couscous.ast.FieldAccessNode.fieldAccess;
import static org.zwobble.couscous.ast.FieldDeclarationNode.field;
import static org.zwobble.couscous.ast.FormalTypeParameterNode.formalTypeParameter;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeTypeParameters;
import static org.zwobble.couscous.frontends.java.FreeVariables.findFreeVariables;
import static org.zwobble.couscous.types.Types.erasure;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.*;

public class ClosureGenerator {
    public static GeneratedClosure classWithCapture(
        Scope scope,
        ClassNode classNode
    ) {
        InsertionOrderSet<TypeParameter> freeTypeParameters = InsertionOrderSet.copyOf(
            findFreeTypeParameters(classNode));

        InsertionOrderSet<ReferenceNode> freeVariables = InsertionOrderSet.copyOf(Iterables.filter(
            findFreeVariables(ExtraLists.concat(
                classNode.getFields(),
                classNode.getMethods())),
            // TODO: check this references work correctly in anonymous classes
            variable -> !isThisReference(classNode.getName(), variable)));
        InsertionOrderSet<CapturedVariable> capturedVariables = InsertionOrderSet.copyOf(transform(
            freeVariables,
            freeVariable -> new CapturedVariable(freeVariable, fieldForCapture(freeVariable))));
        Iterable<FieldDeclarationNode> captureFields = transform(capturedVariables, capture -> capture.field);

        List<FieldDeclarationNode> fields = ImmutableList.copyOf(concat(
            classNode.getFields(),
            captureFields));

        if (!classNode.getStaticConstructor().isEmpty()) {
            throw new RuntimeException("Class has unexpected static constructor");
        }

        ClassNode generatedClass = new ClassNode(
            classNode.getName(),
            // TODO: generate fresh type parameter and replace in body
            ExtraLists.concat(lazyMap(freeTypeParameters, parameter -> formalTypeParameter(parameter)), classNode.getTypeParameters()),
            classNode.getSuperTypes(),
            fields,
            list(),
            buildConstructor(scope, classNode.getName(), capturedVariables, classNode.getConstructor()),
            eagerMap(classNode.getMethods(), method ->
                method.mapBody(body -> replaceCaptureReferences(classNode.getName(), body, capturedVariables))),
            list()
        );
        return new GeneratedClosure(generatedClass, freeTypeParameters, freeVariables);
    }

    private static boolean isThisReference(ScalarType name, ReferenceNode reference) {
        return tryCast(ThisReferenceNode.class, reference)
            .map(node -> node.getType().equals(name))
            .orElse(false);
    }

    private static FieldDeclarationNode fieldForCapture(ReferenceNode freeVariable) {
        return freeVariable.accept(new ReferenceNode.Visitor<FieldDeclarationNode>() {
            @Override
            public FieldDeclarationNode visit(VariableReferenceNode reference) {
                return field(reference.getReferent().getName(), reference.getType());
            }

            @Override
            public FieldDeclarationNode visit(ThisReferenceNode thisReference) {
                Type type = thisReference.getType();
                String name = "this_" + erasure(type).getQualifiedName().replace(".", "__");
                return field(name, type);
            }
        });
    }

    private static List<StatementNode> replaceCaptureReferences(
        ScalarType className,
        List<StatementNode> body,
        InsertionOrderSet<CapturedVariable> freeVariables)
    {
        Map<ExpressionNode, ExpressionNode> replacements = Maps.transformValues(
            Maps.uniqueIndex(freeVariables, variable -> variable.freeVariable),
            freeVariable -> captureAccess(className, freeVariable));
        NodeTransformer transformer = NodeTransformer.replaceExpressions(replacements);
        return eagerFlatMap(body, transformer::transformStatement);
    }

    private static ConstructorNode buildConstructor(
        Scope outerScope,
        ScalarType type,
        InsertionOrderSet<CapturedVariable> freeVariables,
        ConstructorNode existing)
    {
        Scope scope = outerScope.enterConstructor();

        ImmutableList.Builder<FormalArgumentNode> arguments = ImmutableList.builder();
        ImmutableList.Builder<StatementNode> body = ImmutableList.builder();

        for (CapturedVariable variable : freeVariables) {
            FormalArgumentNode argument = scope.formalArgument(variable.field.getName(), variable.field.getType());
            arguments.add(argument);
            body.add(assignStatement(captureAccess(type, variable), reference(argument)));
        }

        arguments.addAll(existing.getArguments());
        body.addAll(existing.getBody());

        return new ConstructorNode(arguments.build(), body.build());
    }

    private static FieldAccessNode captureAccess(ScalarType type, CapturedVariable freeVariable) {
        FieldDeclarationNode field = freeVariable.field;
        return fieldAccess(thisReference(type), field.getName(), field.getType());
    }

    private static class CapturedVariable {
        private final ReferenceNode freeVariable;
        private final FieldDeclarationNode field;

        public CapturedVariable(ReferenceNode freeVariable, FieldDeclarationNode field) {
            this.freeVariable = freeVariable;
            this.field = field;
        }
    }
}
