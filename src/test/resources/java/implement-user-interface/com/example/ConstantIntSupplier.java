package com.example;

import java.util.function.Supplier;

public class ConstantIntSupplier implements IntSupplier {
    public static int value() {
        IntSupplier supplier = new ConstantIntSupplier();
        return supplier.get();
    }
    
    public int get() {
        return 42;
    }
}
