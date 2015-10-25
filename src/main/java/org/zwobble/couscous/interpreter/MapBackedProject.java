package org.zwobble.couscous.interpreter;

import java.util.Map;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.values.ConcreteType;
import com.google.common.collect.ImmutableMap;

public class MapBackedProject implements Project {
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<TypeName, ConcreteType> classes;
        
        private Builder() {
            classes = ImmutableMap.builder();
        }
        
        public Builder addClass(ConcreteType clazz) {
            classes.put(clazz.getName(), clazz);
            return this;
        }
        
        public Builder addClasses(Iterable<ConcreteType> classes) {
            for (ConcreteType clazz : classes) {
                addClass(clazz);
            }
            return this;
        }
        
        public Project build() {
            return new MapBackedProject(classes.build());
        }
    }
    private Map<TypeName, ConcreteType> classes;
    
    public MapBackedProject(Map<TypeName, ConcreteType> classes) {
        this.classes = classes;
    }
    
    @Override
    public ConcreteType findClass(TypeName name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            throw new IllegalArgumentException("Cannot find class: " + name);
        }
    }
}