package org.zwobble.couscous;

import java.util.Map;

import org.zwobble.couscous.ast.ClassName;
import org.zwobble.couscous.interpreter.values.ConcreteType;

import com.google.common.collect.ImmutableMap;

public class MapBackedProject implements Project {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<ClassName, ConcreteType<?>> classes;
        
        private Builder() {
            classes = ImmutableMap.builder();
        }
        
        public Builder addClass(ConcreteType<?> clazz) {
            classes.put(clazz.getName(), clazz);
            return this;
        }
        
        public Project build() {
            return new MapBackedProject(classes.build());
        }
    }
    
    private Map<ClassName, ConcreteType<?>> classes;

    public MapBackedProject(Map<ClassName, ConcreteType<?>> classes) {
        this.classes = classes;
    }
    
    @Override
    public ConcreteType<?> findClass(ClassName name) {
        return classes.get(name);
    }
}
