package org.zwobble.couscous.ast;

import java.util.List;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

@Builder
@Value
public class ClassNode {
    @Singular
    List<MethodNode> methods;
}
