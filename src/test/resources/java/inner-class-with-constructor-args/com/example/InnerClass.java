package com.example;

public class InnerClass {
    private final int stash;
    
    public static int run() {
        return new InnerClass(21).value();
    }
    
    public InnerClass(int stash) {
        this.stash = stash;
    }
    
    public int value() {
        IntSupplier supplier = new IntSupplier(2);
        return supplier.get();
    }
    
    private class IntSupplier {
        private final int factor;
        
        public IntSupplier(int factor) {
            this.factor = factor;
        }
        
        public int get() {
            return stash * factor;
        }
    }
}
