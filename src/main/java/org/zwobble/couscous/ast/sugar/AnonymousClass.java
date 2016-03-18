package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.types.Type;

import java.util.List;
import java.util.Set;

public class AnonymousClass {
    private final Set<Type> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final List<MethodNode> methods;

    public AnonymousClass(
        Set<Type> superTypes,
        List<FieldDeclarationNode> fields,
        List<MethodNode> methods)
    {

        this.superTypes = superTypes;
        this.fields = fields;
        this.methods = methods;
    }

    public Set<Type> getSuperTypes() {
        return superTypes;
    }

    public List<FieldDeclarationNode> getFields() {
        return fields;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }
}
