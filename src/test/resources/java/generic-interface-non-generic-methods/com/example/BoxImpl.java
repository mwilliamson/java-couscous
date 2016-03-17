package com.example;

public class BoxImpl<T> implements Box<T> {
    private T value;
    
    public T get() {
        return value;
    }
    
    public void set(T value) {
        this.value = value;
    }
}

