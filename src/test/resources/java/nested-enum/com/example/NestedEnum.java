package com.example;

public class NestedEnum {
    public static int value() {
        State state = State.ON;
        if (state == State.ON) {
            return 42;
        } else {
            return 0;
        }
    }
    
    public static enum State {
        ON,
        OFF
    }
}
