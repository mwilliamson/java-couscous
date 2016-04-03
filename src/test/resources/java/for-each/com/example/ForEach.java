package com.example;

import static java.util.Arrays.asList;

public class ForEach {
    public static String value() {
        Iterable<String> values = asList("a", "b", "c");
        String result = "";
        
        for (String value : values) {
            result += value;
        }
        
        return result;
    }
}
