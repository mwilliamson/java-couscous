package org.zwobble.couscous;

import org.zwobble.couscous.interpreter.values.IntegerInterpreterValue;

public class JavaProject {
    public static MapBackedProject.Builder builder() {
        return MapBackedProject.builder()
            .addClass(IntegerInterpreterValue.TYPE);
    }
}
