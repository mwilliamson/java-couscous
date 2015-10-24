package org.zwobble.couscous.ast;

import lombok.Value;

@Value(staticConstructor="annotation")
public class AnnotationNode {
    TypeName type;
}
