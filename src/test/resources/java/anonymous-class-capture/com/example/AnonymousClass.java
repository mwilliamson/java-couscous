package com.example;

import java.util.function.Supplier;

public class AnonymousClass {
    public static int value(int x) {
        IntSupplier supplier = new IntSupplier() {
            @Override
            public int get() {
                return x;
            }
        };
        
        return supplier.get();
    }
}
