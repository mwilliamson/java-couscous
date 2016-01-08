package org.zwobble.couscous.ast.sugar;

import org.zwobble.couscous.ast.FieldDeclarationNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.TypeName;

import java.util.List;
import java.util.Set;

public class AnonymousClass {
    private final Set<TypeName> superTypes;
    private final List<FieldDeclarationNode> fields;
    private final List<MethodNode> methods;

    public AnonymousClass(
        Set<TypeName> superTypes,
        List<FieldDeclarationNode> fields,
        List<MethodNode> methods)
    {

        this.superTypes = superTypes;
        this.fields = fields;
        this.methods = methods;
    }

    public Set<TypeName> getSuperTypes() {
        return superTypes;
    }

    public List<FieldDeclarationNode> getFields() {
        return fields;
    }

    public List<MethodNode> getMethods() {
        return methods;
    }
}
