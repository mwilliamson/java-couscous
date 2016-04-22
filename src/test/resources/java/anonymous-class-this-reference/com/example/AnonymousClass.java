package com.example;

public class AnonymousClass {
    public static int value() {
        IntSupplier supplier = new IntSupplier() {
            @Override
            public int get() {
                return 21 * factor();
            }
            
            private int factor() {
                return 2;
            }
        };
        
        return supplier.get();
    }
}
