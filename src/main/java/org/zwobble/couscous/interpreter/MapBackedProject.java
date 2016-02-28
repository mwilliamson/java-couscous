package org.zwobble.couscous.interpreter;

import com.google.common.collect.ImmutableMap;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.types.InterpreterType;

import java.util.Map;

public class MapBackedProject implements Project {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private final ImmutableMap.Builder<TypeName, InterpreterType> classes;
        
        private Builder() {
            classes = ImmutableMap.builder();
        }
        
        public Builder addClass(InterpreterType clazz) {
            classes.put(clazz.getName(), clazz);
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

    private Map<TypeName, InterpreterType> classes;
    
    public MapBackedProject(Map<TypeName, InterpreterType> classes) {
        this.classes = classes;
    }
    
    @Override
    public InterpreterType findClass(TypeName name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        } else {
            throw new IllegalArgumentException("Cannot find class: " + name);
        }
    }
}