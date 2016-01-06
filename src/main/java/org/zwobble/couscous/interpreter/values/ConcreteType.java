package org.zwobble.couscous.interpreter.values;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.zwobble.couscous.ast.ClassNode;
import org.zwobble.couscous.ast.FormalArgumentNode;
import org.zwobble.couscous.ast.MethodNode;
import org.zwobble.couscous.ast.TypeName;
import org.zwobble.couscous.interpreter.*;
import org.zwobble.couscous.util.Casts;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import static java.util.stream.Collectors.toMap;
import static org.zwobble.couscous.util.ExtraLists.eagerMap;

public class ConcreteType {
    
    public static <T> ConcreteType.Builder<T> builder(Class<T> interpreterValueType, TypeName reference) {
        return new Builder<>(interpreterValueType, reference);
    }
    
    public static <T> ConcreteType.Builder<T> builder(Class<T> interpreterValueType, String name) {
        return builder(interpreterValueType, TypeName.of(name));
    }
    
    public static ConcreteType.Builder<ObjectInterpreterValue> classBuilder(String name) {
        return builder(ObjectInterpreterValue.class, name);
    }
    
    public static class Builder<T> {
        private final ImmutableMap.Builder<String, FieldValue> fields = ImmutableMap.builder();
        private final ImmutableMap.Builder<MethodSignature, MethodValue> methods = ImmutableMap.builder();
        private final ImmutableMap.Builder<MethodSignature, StaticMethodValue> staticMethods = ImmutableMap.builder();
        private final Class<T> interpreterValueType;
        private final TypeName name;
        
        public Builder(Class<T> interpreterValueType, TypeName name) {
            this.interpreterValueType = interpreterValueType;
            this.name = name;
        }

        public Builder<T> field(String name, TypeName type) {
            fields.put(name, new FieldValue(name, type));
            return this;
        }

        public Builder<T> method(String name, List<TypeName> argumentsTypes, BiFunction<Environment, MethodCallArguments<T>, InterpreterValue> method) {
            methods.put(new MethodSignature(name, argumentsTypes), new MethodValue(argumentsTypes, (environment, arguments) -> {
                return Casts.tryCast(interpreterValueType, arguments.getReceiver()).map(typedReceiver -> method.apply(environment, MethodCallArguments.of(typedReceiver, arguments.getPositionalArguments()))).orElseThrow(() -> new RuntimeException("receiver is of wrong type"));
            }));
            return this;
        }
        
        public Builder<T> staticMethod(String name, List<TypeName> argumentsTypes, BiFunction<Environment, PositionalArguments, InterpreterValue> method) {
            staticMethods.put(new MethodSignature(name, argumentsTypes), new StaticMethodValue(argumentsTypes, method));
            return this;
        }
        
        public ConcreteType build() {
            return new ConcreteType(
                name,
                Collections.emptySet(),
                fields.build(),
                new MethodValue(ImmutableList.of(), (environment, arguments) -> InterpreterValues.UNIT),
                methods.build(),
                staticMethods.build());
        }
    }
    
    public static ConcreteType fromNode(ClassNode classNode) {
        Map<String, FieldValue> fields = classNode.getFields().stream().collect(toMap(field -> field.getName(), field -> new FieldValue(field.getName(), field.getType())));
        MethodValue constructor = new MethodValue(getArgumentTypes(classNode.getConstructor().getArguments()), (environment, arguments) -> Executor.callMethod(environment, classNode.getConstructor(), Optional.of(arguments.getReceiver()), arguments.getPositionalArguments()));
        Map<MethodSignature, MethodValue> methods = classNode.getMethods()
            .stream()
            .filter(method -> !method.isStatic())
            .collect(toMap(method -> signature(method), method -> {
                List<TypeName> argumentTypes = getArgumentTypes(method.getArguments());
                return new MethodValue(argumentTypes, (environment, arguments) -> {
                    return Executor.callMethod(environment, method, Optional.of(arguments.getReceiver()), arguments.getPositionalArguments());
                });
            }));
        Map<MethodSignature, StaticMethodValue> staticMethods = classNode.getMethods()
            .stream()
            .filter(method -> method.isStatic())
            .collect(toMap(method -> signature(method), method -> {
                List<TypeName> argumentTypes = getArgumentTypes(method.getArguments());
                return new StaticMethodValue(argumentTypes, (environment, arguments) -> {
                    return Executor.callMethod(environment, method, Optional.empty(), arguments);
                });
            }));
        return new ConcreteType(classNode.getName(), classNode.getSuperTypes(), fields, constructor, methods, staticMethods);
    }

    private static MethodSignature signature(MethodNode method) {
        return new MethodSignature(
            method.getName(),
            eagerMap(method.getArguments(), argument -> argument.getType()));
    }

    private static List<TypeName> getArgumentTypes(List<FormalArgumentNode> arguments) {
        return arguments.stream().map(arg -> arg.getType()).collect(Collectors.toList());
    }
    private final TypeName name;
    private final Set<TypeName> superTypes;
    private final Map<String, FieldValue> fields;
    private final MethodValue constructor;
    private final Map<MethodSignature, MethodValue> methods;
    private final Map<MethodSignature, StaticMethodValue> staticMethods;
    
    private ConcreteType(
        TypeName name,
        Set<TypeName> superTypes,
        Map<String, FieldValue> fields,
        MethodValue constructor,
        Map<MethodSignature, MethodValue> methods,
        Map<MethodSignature, StaticMethodValue> staticMethods)
    {
        this.name = name;
        this.superTypes = superTypes;
        this.fields = fields;
        this.constructor = constructor;
        this.methods = methods;
        this.staticMethods = staticMethods;
    }
    
    public TypeName getName() {
        return name;
    }

    public Set<TypeName> getSuperTypes() {
        return superTypes;
    }

    public Optional<FieldValue> getField(String fieldName) {
        return Optional.ofNullable(fields.get(fieldName));
    }
    
    public InterpreterValue callMethod(Environment environment, InterpreterValue receiver, MethodSignature signature, List<InterpreterValue> arguments) {
        MethodValue method = findMethod(methods, signature, arguments);
        return method.apply(environment, MethodCallArguments.of(receiver, new PositionalArguments(arguments)));
    }
    
    public InterpreterValue callStaticMethod(Environment environment, MethodSignature signature, List<InterpreterValue> arguments) {
        StaticMethodValue method = findMethod(staticMethods, signature, arguments);
        return method.apply(environment, new PositionalArguments(arguments));
    }
    
    public InterpreterValue callConstructor(Environment environment, List<InterpreterValue> arguments) {
        final org.zwobble.couscous.interpreter.values.ObjectInterpreterValue object = new ObjectInterpreterValue(this);
        checkMethodArguments(constructor, arguments);
        constructor.apply(environment, MethodCallArguments.of(object, new PositionalArguments(arguments)));
        return object;
    }
    
    private static <T extends Callable> T findMethod(Map<MethodSignature, T> methods, MethodSignature signature, List<InterpreterValue> arguments) {
        if (!methods.containsKey(signature)) {
            throw new NoSuchMethod(signature);
        }
        final T method = methods.get(signature);
        checkMethodArguments(method, arguments);
        return method;
    }
    
    private static void checkMethodArguments(final Callable method, List<InterpreterValue> arguments) {
        if (method.getArgumentTypes().size() != arguments.size()) {
            throw new WrongNumberOfArguments(method.getArgumentTypes().size(), arguments.size());
        }
        for (int index = 0; index < arguments.size(); index++) {
            final org.zwobble.couscous.ast.TypeName formalArgumentType = method.getArgumentTypes().get(index);
            InterpreterTypes.checkIsInstance(formalArgumentType, arguments.get(index));
        }
    }
    
    @Override
    public String toString() {
        return "ConcreteType<" + name + ">";
    }
}