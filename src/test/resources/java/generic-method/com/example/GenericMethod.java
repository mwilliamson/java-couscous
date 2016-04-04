package com.example;

public class GenericMethod {
    public static String value() {
        return second("one", "two");
    }
    
    private static <T> T second(T first, T second) {
        return second;
    }
}
