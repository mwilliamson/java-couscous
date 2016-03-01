package com.example;

public class StaticNestedClass {
    public static int value() {
        IntSupplier supplier = new IntSupplier();
        return supplier.get();
    }
    
    private static class IntSupplier {
        public int get() {
            return 42;
        }
    }
}
