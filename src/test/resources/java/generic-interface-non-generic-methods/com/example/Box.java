package com.example;

public interface Box<T> {
    T get();
    void set(T value);
}
