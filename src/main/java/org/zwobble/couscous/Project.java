package org.zwobble.couscous;

import org.zwobble.couscous.ast.ClassNode;

public interface Project {
    ClassNode findClass(String name);
}
