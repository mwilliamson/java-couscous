package com.example;

public class WhileFactorial {
    public static int factorial(int n) {
        int result = 1;
        while (n > 1) {
            result *= n;
            --n;
        }
        return result;
    }
}
