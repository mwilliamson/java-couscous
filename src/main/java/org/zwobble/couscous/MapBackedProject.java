package org.zwobble.couscous;

import java.util.Map;

import org.zwobble.couscous.values.ConcreteType;

import com.google.common.collect.ImmutableMap;

public class MapBackedProject implements Project {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<String, ConcreteType<?>> classes;
        
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
    
    private Map<String, ConcreteType<?>> classes;

    public MapBackedProject(Map<String, ConcreteType<?>> classes) {
        this.classes = classes;
    }
    
    @Override
    public ConcreteType<?> findClass(String name) {
        return classes.get(name);
    }
}
