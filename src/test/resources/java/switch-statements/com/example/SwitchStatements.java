package com.example;

public class SwitchStatements {
    public static int simpleReturn(String value) {
        switch (value) {
            case "one":
                return 1;
            default:
                return 0;
        }
    }
    
    public static int simpleFallthroughReturn(String value) {
        switch (value) {
            case "one":
            case "two":
                return 1;
            default:
                return 0;
        }
    }
    
    public static int noDefaultReturn(String value) {
        switch (value) {
            case "one":
                return 1;
        }
        return 0;
    }
}
