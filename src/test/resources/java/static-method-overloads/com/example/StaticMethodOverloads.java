package com.example;

public class StaticMethodOverloads {
    public static int value() {
        return value(21);
    }
    
    public static int value(int x) {
        return x * value(Integer.valueOf(x).toString());
    }

    public static int value(String value) {
        return value.length();
    }
}
