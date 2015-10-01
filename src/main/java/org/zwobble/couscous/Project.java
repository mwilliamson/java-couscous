package org.zwobble.couscous;

import org.zwobble.couscous.values.ConcreteType;

public interface Project {
    ConcreteType<?> findClass(String name);
}
