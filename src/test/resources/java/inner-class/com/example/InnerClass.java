package com.example;

public class InnerClass {
    private final int stash;
    
    public static int run() {
        return new InnerClass(42).value();
    }
    
    public InnerClass(int stash) {
        this.stash = stash;
    }
    
    public int value() {
        IntSupplier supplier = new IntSupplier();
        return supplier.get();
    }
    
    private class IntSupplier {
        public int get() {
            return stash;
        }
    }
}
