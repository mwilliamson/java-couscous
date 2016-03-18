package org.zwobble.couscous.interpreter;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.types.ScalarType;
import org.zwobble.couscous.interpreter.types.InterpreterType;

import java.util.Map;

import static org.zwobble.couscous.types.Types.erasure;

public class MapBackedProject implements Project {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<ScalarType, InterpreterType> classes;
        
        private Builder() {
            classes = ImmutableMap.builder();
        }
        
        public Builder addClass(InterpreterType clazz) {
            classes.put(erasure(clazz.getType()), clazz);
            return this;
        }
        
        public Builder addClasses(Iterable<InterpreterType> classes) {
            for (InterpreterType clazz : classes) {
                addClass(clazz);
            }
            return this;
        }
        
        public Project build() {
            return new MapBackedProject(classes.build());
        }
    }

    private Map<ScalarType, InterpreterType> classes;
    
    public MapBackedProject(Map<ScalarType, InterpreterType> classes) {
        this.classes = classes;
    }
    
    @Override
    public InterpreterType findClass(ScalarType name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            throw new IllegalArgumentException("Cannot find class: " + name);
        }
    }
}