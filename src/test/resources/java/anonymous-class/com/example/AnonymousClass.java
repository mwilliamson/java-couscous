package com.example;

public class AnonymousClass {
    public static int value() {
        IntSupplier supplier = new IntSupplier() {
            @Override
            public int get() {
                return 42;
            }
        };
        
        return supplier.get();
    }
}
