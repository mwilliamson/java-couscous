package org.zwobble.couscous.transforms;

import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.TypeNode;
import org.zwobble.couscous.ast.visitors.NodeTransformer;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.types.Type;
import org.zwobble.couscous.util.ExtraIterables;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.zwobble.couscous.util.Casts.tryCast;
import static org.zwobble.couscous.util.ExtraIterables.lazyMap;
import static org.zwobble.couscous.util.ExtraLists.*;
import static org.zwobble.couscous.util.ExtraMaps.lookup;

public class HoistNestedTypes {
    public static List<TypeNode> hoist(List<TypeNode> declarations) {
        Map<Type, Type> hoisted = new HashMap<>();

        Iterable<TypeNode> types = eagerFlatMap(declarations, declaration ->
            tryCast(ClassNode.class, declaration)
                .map(classDeclaration -> hoistInnerTypes(hoisted, classDeclaration))
                .orElse(ExtraIterables.of(declaration))
        );

        NodeTransformer referenceTransformer = NodeTransformer.builder()
            .transformType(type -> lookup(hoisted, type).orElse(type))
            .build();

        return eagerFlatMap(types, referenceTransformer::transformTypeDeclaration);
    }

    private static Iterable<TypeNode> hoistInnerTypes(Map<Type, Type> hoisted, ClassNode classDeclaration) {
        return cons(
            stripInnerTypes(classDeclaration),
            lazyMap(classDeclaration.getInnerTypes(), innerType -> {
                ScalarType hoistedName = ScalarType.of(
                    classDeclaration.getName().getQualifiedName() + "__" + innerType.getName().getSimpleName()
                );
                hoisted.put(innerType.getName(), hoistedName);
                return hoistType(innerType, hoistedName);
            })
        );
    }

    private static TypeNode stripInnerTypes(ClassNode classDeclaration) {
        return new ClassNode(
            classDeclaration.getName(),
            classDeclaration.getTypeParameters(),
            classDeclaration.getSuperTypes(),
            classDeclaration.getFields(),
            classDeclaration.getStaticConstructor(),
            classDeclaration.getConstructor(),
            classDeclaration.getMethods(),
            list()
        );
    }

    private static TypeNode hoistType(TypeNode innerType, ScalarType hoistedName) {
        return innerType.rename(hoistedName);
    }

//    private TypeNode readNestedTypeDeclaration(TypeDeclarationBody.Builder body, TypeDeclaration typeDeclaration) {
//        // TODO: can we remove duplication of scope creation with readTypeDeclaration()?
//        Scope scope = topScope.enterClass(typeNode.getName());
//        return tryCast(ClassNode.class, typeNode)
//            .filter(node -> !Modifier.isStatic(typeDeclaration.getModifiers()))
//            .<TypeNode>map(node -> {
//                GeneratedClosure closure = classWithCapture(scope, node);
////                Type type = node.getTypeParameters().isEmpty()
////                    ? typeNode.getName()
////                    : parameterizedType(typeNode.getName(), eagerMap(node.getTypeParameters()));
//                Type type = typeNode.getName();
//                // TODO: method type parameters
//                List<FormalArgumentNode> methodArguments = eagerMap(
//                    node.getConstructor().getArguments(),
//                    // TODO: use scope of outer class
//                    argument -> scope.formalArgument(argument.getName(), argument.getType()));
//                MethodNode method = MethodNode.builder("create_" + typeDeclaration.getName().getIdentifier())
//                    .arguments(methodArguments)
//                    .returns(type)
//                    .statement(returns(closure.generateConstructor(lazyMap(methodArguments, argument -> reference(argument)))))
//                    .build();
//                body.addMethod(method);
//                return closure.getClassNode();
//            })
//            .orElse(typeNode);
//    }
}
