package org.zwobble.couscous.tests.interpreter;

import java.util.Map;

import org.zwobble.couscous.Project;
import org.zwobble.couscous.values.ConcreteType;

public class MapBackedProject implements Project {
    private Map<String, ConcreteType<?>> classes;

    public MapBackedProject(Map<String, ConcreteType<?>> classes) {
        this.classes = classes;
    }
    
    @Override
    public ConcreteType<?> findClass(String name) {
        return classes.get(name);
    }
}
