package com.example;

public class AnonymousClass {
    public static String value() {
        return get("Hello");
    }
    
    private static <T> T get(T value) {
        Supplier<T> supplier = new Supplier<T>() {
            @Override
            public T get() {
                return value;
            }
        };
        
        return supplier.get();
    }
}
