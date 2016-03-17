package com.example;

public class GenericInterface {
    public static String value() {
        Box<String> box = new BoxImpl<>();
        box.set("Hello");
        
        return box.get();
    }
}
