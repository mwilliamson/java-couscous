package com.example;

public class GenericInterface {
    public static String value() {
        Supplier<String> supplier = new Supplier<String>() {
            @Override
            public String get() {
                return "Hello";
            }
        };
        
        return supplier.get();
    }
}
