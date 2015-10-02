package org.zwobble.couscous;

import java.util.Map;

import org.zwobble.couscous.values.ConcreteType;

import lombok.Builder;
import lombok.Singular;

@Builder
public class MapBackedProject implements Project {
    @Singular(value="addClass")
    private Map<String, ConcreteType<?>> classes;

    public MapBackedProject(Map<String, ConcreteType<?>> classes) {
        this.classes = classes;
    }
    
    @Override
    public ConcreteType<?> findClass(String name) {
        return classes.get(name);
    }
}
