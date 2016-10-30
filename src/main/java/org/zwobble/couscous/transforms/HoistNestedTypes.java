package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.*;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.frontends.java.GeneratedClosure;
import org.zwobble.couscous.frontends.java.Scope;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.types.Types;
import org.zwobble.couscous.util.ExtraSets;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.zwobble.couscous.ast.MethodCallNode.methodCall;
import static org.zwobble.couscous.ast.ReturnNode.returns;
import static org.zwobble.couscous.ast.ThisReferenceNode.thisReference;
import static org.zwobble.couscous.ast.VariableReferenceNode.reference;
import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.*;
import static org.zwobble.couscous.util.ExtraLists.*;

public class HoistNestedTypes {
    public static List<TypeNode> hoist(List<TypeNode> declarations) {
        return new HoistNestedTypes().hoistTypes(declarations);
    }

    private final Scope topScope = Scope.create().temporaryPrefix("_couscous_hoist_nested_types");

    private List<TypeNode> hoistTypes(List<TypeNode> declarations) {
        List<HoistResult> hoistResults = eagerMap(declarations, declaration -> hoistInnerTypes(declaration));

        Iterable<HoistedType> hoistedTypes = lazyFlatMap(hoistResults, hoisted -> hoisted.hoistedTypes);
        Set<Type> capturingTypes = ExtraSets.copyOf(
            lazyMap(
                lazyFilter(hoistedTypes, hoisted -> hoisted.method.isPresent()),
                hoistedType -> hoistedType.type.getName()
            )
        );

        NodeTransformer constructorCallTransformer = NodeTransformer.builder()
            .transformExpression(expression -> transformExpression(expression, capturingTypes))
            .build();

        List<TypeNode> types = eagerFlatMap(
            hoistResults,
            result -> cons(
                addMethods(
                    only(NodeTransformer.apply(constructorCallTransformer, list(result.outerType.stripInnerTypes()))),
                    eagerFlatMap(result.hoistedTypes, hoisted -> iterable(hoisted.method))
                ),
                lazyMap(result.hoistedTypes, hoisted -> hoisted.type)
            )
        );

        NodeTransformer referenceTransformer = NodeTransformer.builder()
            .transformType(this::transformType)
            .build();

        return NodeTransformer.apply(referenceTransformer, types);
    }

    private Optional<ExpressionNode> transformExpression(ExpressionNode expression, Set<Type> capturingTypes) {
        return tryCast(ConstructorCallNode.class, expression)
            .filter(call -> capturingTypes.contains(Types.erasure(call.getType())))
            .map(call -> methodCall(
                thisReference(outerType(Types.erasure(call.getType()))),
                createMethodName(Types.erasure(call.getType())),
                (List<ExpressionNode>)call.getArguments(),
                call.getType()
            ));
    }

    private Type transformType(Type type) {
        if (isInnerType(type)) {
            return hoistedTypeName((ScalarType) type);
        } else {
            return type.transformSubTypes(this::transformType);
        }
    }

    private static final Pattern INNER_TYPE_QUALIFIER_REGEX = Pattern.compile(".*\\.[A-Z][^.]*$");

    private boolean isInnerType(Type type) {
        // TODO: implement separate type classes for top-level and inner types rather than using this heuristic

        return tryCast(ScalarType.class, type)
            .flatMap(scalarType -> scalarType.getQualifier())
            .map(qualifier -> INNER_TYPE_QUALIFIER_REGEX.matcher(qualifier).matches())
            .orElse(false);
    }

    private Type outerType(ScalarType type) {
        // TODO: implement separate type classes for top-level and inner types

        return ScalarType.of(type.getQualifier().get());
    }

    private static Type hoistedTypeName(ScalarType type) {
        return ScalarType.of(type.getQualifier().get() + "__" + type.getSimpleName());
    }

    private HoistResult hoistInnerTypes(TypeNode declaration) {
        return new HoistResult(
            declaration,
            eagerMap(declaration.getInnerTypes(), this::hoistType)
        );
    }

    private static TypeNode addMethods(TypeNode declaration, List<MethodNode> methods) {
        if (methods.isEmpty()) {
            return declaration;
        } else if (declaration instanceof ClassNode) {
            ClassNode classDeclaration = (ClassNode) declaration;
            return new ClassNode(
                classDeclaration.getName(),
                classDeclaration.getTypeParameters(),
                classDeclaration.getSuperTypes(),
                classDeclaration.getFields(),
                classDeclaration.getStaticConstructor(),
                classDeclaration.getConstructor(),
                concat(classDeclaration.getMethods(), methods),
                classDeclaration.getInnerTypes()
            );
        } else {
            throw new UnsupportedOperationException("Cannot add methods to non-class node");
        }
    }

    private HoistedType hoistType(TypeNode typeNode) {
        // TODO: we need to hoist inner types of the inner types, specifically *after* the initial hoist (to make closures easier to generate)
        // TODO: can we remove duplication of scope creation with readTypeDeclaration()?
        Scope scope = topScope.enterClass(typeNode.getName());
        return tryCast(ClassNode.class, typeNode)
            .flatMap(node -> {
                // TODO: don't generate closure if not necessary (i.e. it's an explicit or implicit static inner class)
                GeneratedClosure closure = ClosureGenerator.classWithCapture(scope, node);
//                Type type = node.getTypeParameters().isEmpty()
//                    ? typeNode.getName()
//                    : parameterizedType(typeNode.getName(), eagerMap(node.getTypeParameters()));
                if (closure.hasCaptures()) {
                    ScalarType type = typeNode.getName();
                    // TODO: method type parameters
                    List<FormalArgumentNode> methodArguments = eagerMap(
                        node.getConstructor().getArguments(),
                        // TODO: use scope of outer class
                        argument -> scope.formalArgument(argument.getName(), argument.getType()));
                    MethodNode method = MethodNode.builder(createMethodName(type))
                        .arguments(methodArguments)
                        .returns(type)
                        .statement(returns(closure.generateConstructor(lazyMap(methodArguments, argument -> reference(argument)))))
                        .build();
                    return Optional.of(new HoistedType(
                        closure.getClassNode(),
                        Optional.of(method)
                    ));
                } else {
                    return Optional.empty();
                }
            })
            .orElse(new HoistedType(typeNode, Optional.empty()));
    }

    private String createMethodName(ScalarType type) {
        return "create_" + type.getSimpleName();
    }

    private static class HoistResult {
        private final TypeNode outerType;
        private final List<HoistedType> hoistedTypes;

        private HoistResult(TypeNode outerType, List<HoistedType> hoistedTypes) {
            this.outerType = outerType;
            this.hoistedTypes = hoistedTypes;
        }
    }

    private static class HoistedType {
        private final TypeNode type;
        private final Optional<MethodNode> method;

        private HoistedType(TypeNode type, Optional<MethodNode> method) {
            this.type = type;
            this.method = method;
        }
    }
}
